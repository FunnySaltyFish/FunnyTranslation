package com.funny.translation.translate.network

import android.util.Log
import com.azhon.appupdate.base.BaseHttpDownloadManager
import com.azhon.appupdate.listener.OnDownloadListener
import com.azhon.appupdate.utils.Constant
import com.azhon.appupdate.utils.FileUtil
import com.azhon.appupdate.utils.LogUtil
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class UpdateDownloadManager(private val downloadPath: String) :
    BaseHttpDownloadManager() {
    private var apkUrl: String? = null
    private var apkName: String? = null
    private var shutdown = false
    private var listener: OnDownloadListener? = null
    private val executor: ThreadPoolExecutor = ThreadPoolExecutor(1, 1,
        0L, TimeUnit.SECONDS, LinkedBlockingQueue()
    ) { r ->
        val thread = Thread(r)
        thread.name = Constant.THREAD_NAME
        thread
    }

    override fun download(apkUrl: String, apkName: String, listener: OnDownloadListener) {
        this.apkUrl = apkUrl
        this.apkName = apkName
        this.listener = listener
        executor.execute(runnable)
    }

    override fun cancel() {
        shutdown = true
    }

    override fun release() {
        listener = null
        executor.shutdown()
    }

    private val runnable = Runnable { //删除之前的安装包
        if (FileUtil.fileExists(downloadPath, apkName)) {
            FileUtil.delete(downloadPath, apkName)
        }
        fullDownload()
    }

    /**
     * 全部下载
     */
    private fun fullDownload() {
        if (listener != null) listener!!.start()
        try {
            val url = URL(apkUrl)
            val con: HttpURLConnection = url.openConnection() as HttpURLConnection
            con.setRequestMethod("GET")
            con.setReadTimeout(Constant.HTTP_TIME_OUT)
            con.setConnectTimeout(Constant.HTTP_TIME_OUT)
            con.setRequestProperty("Accept-Encoding", "identity")
            Log.d(TAG, "fullDownload: ${con.responseCode}")
            if (con.responseCode == HttpURLConnection.HTTP_OK) {
                val `is`: InputStream = con.getInputStream()
                val length: Int = con.getContentLength()
                var len: Int
                //当前已下载完成的进度
                var progress = 0
                val buffer = ByteArray(1024 * 2)
                val file: File = FileUtil.createFile(downloadPath, apkName)
                val stream = FileOutputStream(file)
                while (`is`.read(buffer).also { len = it } != -1 && !shutdown) {
                    //将获取到的流写入文件中
                    stream.write(buffer, 0, len)
                    progress += len
                    if (listener != null) listener!!.downloading(length, progress)
                }
                if (shutdown) {
                    //取消了下载 同时再恢复状态
                    shutdown = false
                    LogUtil.d(TAG, "fullDownload: 取消了下载")
                    if (listener != null) listener!!.cancel()
                } else {
                    if (listener != null) listener!!.done(file)
                }
                //完成io操作,释放资源
                stream.flush()
                stream.close()
                `is`.close()
                //重定向
            } else if (con.responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                con.responseCode == HttpURLConnection.HTTP_MOVED_TEMP || con.responseCode == 307
            ) {
                apkUrl = con.getHeaderField("Location")
                con.disconnect()
                LogUtil.d(
                    TAG,
                    "fullDownload: 当前地址是重定向Url，定向后的地址：$apkUrl"
                )
                fullDownload()
            } else {
                if (listener != null) listener!!.error(SocketTimeoutException("下载失败：Http ResponseCode = " + con.getResponseCode()))
            }
            con.disconnect()
        } catch (e: Exception) {
            if (listener != null) listener!!.error(e)
            e.printStackTrace()
        }
    }

    companion object {
        private const val TAG: String = "HttpDownloadManager"
    }
}