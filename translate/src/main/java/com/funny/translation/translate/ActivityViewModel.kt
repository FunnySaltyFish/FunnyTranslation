package com.funny.translation.translate

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.azhon.appupdate.config.UpdateConfiguration
import com.azhon.appupdate.manager.DownloadManager
import com.funny.translation.codeeditor.extensions.externalCache
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.network.TransNetwork
import com.funny.translation.translate.network.UpdateDownloadManager
import com.funny.translation.translate.utils.ApplicationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DecimalFormat


class ActivityViewModel : ViewModel() {
    var lastBackTime : Long = 0
    var hasCheckedUpdate = false
    companion object{
        const val TAG = "ActivityVM"
    }

    private val Int.toSize : String
        get() {
            val GB = 1024 * 1024 * 1024
            val MB = 1024 * 1014
            val KB = 1024
            val df = DecimalFormat("0.00")
            val size = this
            return when {
                size / GB >= 1 -> df.format(size / GB.toFloat()) + "GB"
                size / MB >= 1 -> df.format(size / MB.toFloat()) + "MB"
                size / KB >= 1 -> df.format(size / KB.toFloat()) + "KB"
                else -> size.toString() + "B"
            }
        }

    suspend fun checkUpdate(context : Context){
        if(hasCheckedUpdate)return
        kotlin.runCatching {
            val manager = DownloadManager.getInstance(context)
            withContext(Dispatchers.IO){
                val versionCode = ApplicationUtil.getAppVersionCode(FunnyApplication.ctx)
                Log.d(TAG, "checkUpdate: VersionCode:$versionCode")
                val channel = DataSaverUtils.readData(Consts.KEY_APP_CHANNEL, "stable")
                val updateInfo = TransNetwork.appUpdateService.getUpdateInfo(versionCode, channel)
                Log.i(TAG, "checkUpdate: $updateInfo")
                if (updateInfo.should_update){
                    val configuration = UpdateConfiguration().apply {
                        httpManager = UpdateDownloadManager(FunnyApplication.ctx.externalCache.absolutePath)
                        isForcedUpgrade = updateInfo.force_update == true
                    }

                    manager.setApkName("update_apk.apk")
                        .setApkUrl(updateInfo.apk_url)
                        .setApkMD5(updateInfo.apk_md5)
                        .setSmallIcon(R.drawable.ic_launcher)
                        //非必须参数
                        .setConfiguration(configuration)
                        //设置了此参数，那么会自动判断是否需要更新弹出提示框
                        .setApkVersionCode(updateInfo.version_code!!)
                        .setApkDescription(updateInfo.update_log)
                        .setApkVersionName(updateInfo.version_name)
                        .setApkSize(updateInfo.apk_size!!.toSize)
                    withContext(Dispatchers.Main){
                        manager.download()
                    }
                }
            }
            hasCheckedUpdate = true
        }.onFailure {
            it.printStackTrace()
        }

    }

}