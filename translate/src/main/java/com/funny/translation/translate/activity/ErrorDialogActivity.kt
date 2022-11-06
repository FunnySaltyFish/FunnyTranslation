package com.funny.translation.translate.activity

import android.os.Bundle
import android.os.Process
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.AppConfig
import com.funny.translation.BaseApplication
import com.funny.translation.helper.toastOnUi
import com.funny.translation.network.OkHttpUtils
import com.funny.translation.network.ServiceCreator
import com.funny.translation.translate.ui.theme.TransTheme
import com.funny.translation.translate.R
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
        
        setContent { 
            TransTheme {
                ErrorDialog()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ErrorDialog() {
        var showDialog by remember {
            mutableStateOf(true)
        }
        var actionDesc by remember {
            mutableStateOf("")
        }
        var contact by remember {
            mutableStateOf("")
        }
        if (showDialog) 
            AlertDialog(
                title = {
                    Text("糟糕")    
                },
                text = {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(
                            rememberScrollState()
                        )) {
                        TextField(value = actionDesc, onValueChange = { actionDesc = it}, placeholder = {
                            Text(text = "（可选）简易描述下您的操作吧~")
                        })
                        Spacer(modifier = Modifier.height(4.dp))
                        TextField(value = contact, onValueChange = { contact = it}, placeholder = {
                            Text(text = "（可选）您的联系方式（如qq xxx）")
                        })
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = """
                                应用程序发生了崩溃，我们建议您发送崩溃报告以反馈。
                                
                                为什么需要崩溃报告？
                                应用崩溃对于使用者和开发者来说都是相当大的灾难，我们都希望将其修复。但是，没有报错原因的崩溃，犹如坏了的无法打开的黑盒子，你只知道出现问题，却不知道是什么问题、问题在哪，难以完成修复。因此，还望您可以提供病症，我们才可以对症下药。
                                               
                                具体原因如下:
                                """.trimLineStart + crashMessage,
                            fontSize = 12.sp,
                            lineHeight = 13.sp
                        )
                    }
                    
                },
                onDismissRequest = { },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                        runBlocking(Dispatchers.IO) { sendCrashReport(actionDesc, contact) }
                        destroy()
                    }) {
                        Text(stringResource(id = R.string.send_report))
                    }            
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showDialog = false 
                        destroy()
                    }) {
                        Text(stringResource(id = R.string.cancel))
                    }
                }
            )
    }

    private fun destroy() {
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }

    private fun sendCrashReport(actionDesc: String, contact: String){
        crashMessage?.let {
            kotlin.runCatching {
                val desc = URLEncoder.encode(actionDesc, "utf-8")
                val conc = URLEncoder.encode(contact, "utf-8")
                val url = OkHttpUtils.removeExtraSlashOfUrl("${ServiceCreator.BASE_URL}/api/report_crash")
                val postData = "contact=$conc&action_desc=$desc&text=${URLEncoder.encode(it,"utf-8")}&version=${BaseApplication.getLocalPackageInfo()?.versionName}(${AppConfig.versionCode})"
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
