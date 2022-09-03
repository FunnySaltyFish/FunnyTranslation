package com.funny.translation.translate.activity

import android.os.Bundle
import android.os.Process
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.funny.translation.BaseApplication
import com.funny.translation.network.OkHttpUtils
import com.funny.translation.network.ServiceCreator
import com.funny.translation.translate.extentions.trimLineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                GlobalScope.launch {
                    withContext(Dispatchers.IO){
                        reportCrash()
                    }
                    destroy()
                }
            }
            .setNegativeButton(
                "退出"
            ) { _,_ ->
                destroy()
            }
            .create()
        dialog.show()
    }

    private fun reportCrash(){
        crashMessage?.let {
            try {
                val packageInfo = BaseApplication.getLocalPackageInfo()
                OkHttpUtils.postForm(
                    url = "${ServiceCreator.BASE_URL}/api/report_crash",
                    form = hashMapOf(
                        "text" to it,
                        "version" to "${packageInfo?.versionName}(${packageInfo?.versionCode})"
                    )
                )
            }catch (e:Exception){
                Toast.makeText(this,"发送失败！",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun destroy() {
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }
}
