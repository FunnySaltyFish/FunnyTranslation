package com.funny.translation.translate.activity

import android.os.Bundle
import android.os.Process
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.funny.translation.AppConfig
import com.funny.translation.BaseApplication
import com.funny.translation.helper.toastOnUi
import com.funny.translation.network.OkHttpUtils
import com.funny.translation.network.ServiceCreator
import com.funny.translation.translate.extentions.trimLineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.system.exitProcess


class ErrorDialogActivity : AppCompatActivity() {
    private var crashMessage : String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crashMessage = intent.getStringExtra("CRASH_MESSAGE")

        val dialog = AlertDialog.Builder(this)
            .setTitle("抱歉，应用程序发生了崩溃！")
            .setMessage(
                """
                应用程序发生了崩溃，我们建议您发送崩溃报告以反馈。
                
                为什么需要崩溃报告？
                应用崩溃对于使用者和开发者来说都是相当大的灾难，我们都希望将其修复。但是，没有报错原因的崩溃，犹如坏了的无法打开的黑盒子，你只知道出现问题，却不知道是什么问题、问题在哪，难以完成修复。因此，还望您可以提供病症，我们才可以对症下药。
                               
                具体原因如下:\n
                """.trimLineStart  + crashMessage
            )
            .setPositiveButton(
                "发送报告"
            ) { _,_ ->
                runBlocking(Dispatchers.IO) { sendCrashReport() }
                Log.d("ErrorHandler", "错误报告发送完毕！ ")
                destroy()
            }
            .setNegativeButton(
                "退出"
            ) { _,_ ->
                destroy()
            }
            .create()
        dialog.show()
    }

    private fun destroy() {
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }

    private fun sendCrashReport(){
        crashMessage?.let {
            kotlin.runCatching {
                val url = OkHttpUtils.removeExtraSlashOfUrl("${ServiceCreator.BASE_URL}/api/report_crash")
                val postData = "text=${URLEncoder.encode(it,"utf-8")}&version=${BaseApplication.getLocalPackageInfo()?.versionName}(${AppConfig.versionCode})"
                doPost(url, postData)
            }.onFailure {
                toastOnUi("发送错误报告失败，请联系开发者提交错误信息")
                it.printStackTrace()
            }
        }
    }

    // OkHttp 的连接池已经被关了，这里手动 HttpUrlConnection 实现 post 请求
    private fun doPost(url: String, postData: String){
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        conn.setRequestProperty("Content-Length", postData.length.toString())
        conn.useCaches = false

        DataOutputStream(conn.outputStream).use { it.writeBytes(postData) }

        Log.d("doPost", "doPost:url: $url conn.code: ${conn.responseCode}")
        when (conn.responseCode){
            301, 302, 307, 308 -> doPost(conn.getHeaderField("Location"), postData)
            else -> BufferedReader(InputStreamReader(conn.inputStream)).use { br ->
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    println(line)
                }
            }
        }
    }
}
