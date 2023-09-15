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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.funny.translation.AppConfig
import com.funny.translation.helper.ApplicationUtil
import com.funny.translation.helper.toastOnUi
import com.funny.translation.network.OkHttpUtils
import com.funny.translation.network.ServiceCreator
import com.funny.translation.translate.R
import com.funny.translation.translate.extentions.trimLineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
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
        lifecycleScope.launch {
            if (crashMessage != null) {
                saveCrashMessage(crashMessage!!)
            }
        }
        
        setContent { 
            // TransTheme {
                ErrorDialog()
            // }
        }
    }

    private fun saveCrashMessage(msg: String){
        val file = this.getExternalFilesDir("crash_logs")
        if (!file?.exists()!!) {
            file.mkdirs()
        }
        // 文件名： CrashLog_时间.txt
        val fileName = "CrashLog_" + System.currentTimeMillis() + ".txt"
        val outputFile = File(file, fileName)
        outputFile.writeText(msg)
    }

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
                    Text(stringResource(R.string.oops))
                },
                text = {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(
                            rememberScrollState()
                        )) {
                        TextField(value = actionDesc, onValueChange = { actionDesc = it}, placeholder = {
                            Text(text = stringResource(R.string.describe_your_operation))
                        })
                        Spacer(modifier = Modifier.height(4.dp))
                        TextField(value = contact, onValueChange = { contact = it}, placeholder = {
                            Text(text = stringResource(R.string.your_contact))
                        })
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.tip_app_crash) + crashMessage,
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
                val postData = hashMapOf(
                    "contact" to conc,
                    "action_desc" to desc,
                    "text" to it.trimLineStart,
                    "version" to "${ApplicationUtil.getAppVersionName(this)}(${AppConfig.versionCode})"
                ).map { "${it.key}=${URLEncoder.encode(it.value, "utf-8")}" }.joinToString("&")
                doPost(url, postData)
            }.onFailure {
                toastOnUi(getString(R.string.err_send_crash_report))
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
