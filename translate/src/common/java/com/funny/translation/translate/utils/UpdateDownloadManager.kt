package com.funny.translation.translate.network

import com.azhon.appupdate.base.BaseHttpDownloadManager
import com.azhon.appupdate.listener.OnDownloadListener
import com.azhon.appupdate.utils.Constant
import com.azhon.appupdate.utils.FileUtil
import com.azhon.appupdate.utils.LogUtil
import com.funny.translation.network.OkHttpUtils
import java.io.File
import java.io.FileOutputStream
import java.net.SocketTimeoutException
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
        downloadByOkHttp()
    }

    private fun downloadByOkHttp() {
        apkUrl ?: return
        try {
            listener?.start()
            val response = OkHttpUtils.getResponse(apkUrl!!)
            if (response.isSuccessful) {
                response.body!!.byteStream().use { inputStream ->
                    val length: Int = response.body!!.contentLength().toInt()
                    var len: Int
                    //当前已下载完成的进度
                    var progress = 0
                    val buffer = ByteArray(1024 * 2)
                    val file: File = FileUtil.createFile(downloadPath, apkName)
                    val stream = FileOutputStream(file)
                    while (inputStream.read(buffer).also { len = it } != -1 && !shutdown) {
                        //将获取到的流写入文件中
                        stream.write(buffer, 0, len)
                        progress += len
                        listener?.downloading(length, progress)
                    }
                    if (shutdown) {
                        //取消了下载 同时再恢复状态
                        shutdown = false
                        LogUtil.d(TAG, "fullDownload: 取消了下载")
                        listener?.cancel()
                    } else {
                        listener?.done(file)
                    }
                    //完成io操作,释放资源
                    stream.flush()
                    stream.close()
                }
            } else {
                listener?.error(SocketTimeoutException("下载失败：Http ResponseCode = " + response.code))
            }
        } catch (e: Exception) {
            listener?.error(e)
            e.printStackTrace()
        }
    }

    companion object {
        private const val TAG: String = "HttpDownloadManager"
    }
}