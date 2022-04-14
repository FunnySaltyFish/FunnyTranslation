package com.funny.translation.translate

import android.content.Context
import android.os.Bundle
import android.os.Process
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.funny.translation.network.OkHttpUtils
import com.funny.translation.network.ServiceCreator
import com.funny.translation.translate.extentions.trimLineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess


class ErrorDialogActivity : AppCompatActivity() {
    private var crashMessage : String? = ""

    companion object {
        const val KEY_CRASH_MESSAGE = "crash_message"
    }

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
                reportCrashInBackground()
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

    /**
     * 调用 WorkManager 在后台上传 CrashReport
     */
    private fun reportCrashInBackground(){
        val workRequest = OneTimeWorkRequestBuilder<UploadCrashWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setInputData(workDataOf(KEY_CRASH_MESSAGE to crashMessage))
            .build()
        WorkManager.getInstance(FunnyApplication.ctx).enqueue(workRequest)
    }

    private fun destroy() {
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }

    class UploadCrashWorker(appContext:Context, workerParameters: WorkerParameters): CoroutineWorker(appContext, workerParameters){
        override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
            val crashMessage: String? = inputData.getString(KEY_CRASH_MESSAGE)
            crashMessage ?: return@withContext Result.success()
            return@withContext try {
                val packageInfo = FunnyApplication.getLocalPackageInfo()
                OkHttpUtils.postForm(
                    url = "${ServiceCreator.BASE_URL}/api/report_crash",
                    form = hashMapOf(
                        "text" to crashMessage,
                        "version" to "${packageInfo?.versionName}(${packageInfo?.versionCode})"
                    )
                )
                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }

}
