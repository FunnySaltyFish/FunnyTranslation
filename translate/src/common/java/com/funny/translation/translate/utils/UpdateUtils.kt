package com.funny.translation.translate.utils

import android.content.Context
import android.util.Log
import com.azhon.appupdate.config.UpdateConfiguration
import com.azhon.appupdate.manager.DownloadManager
import com.funny.translation.Consts
import com.funny.translation.helper.ApplicationUtil
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.externalCache
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.R
import com.funny.translation.translate.network.TransNetwork
import com.funny.translation.translate.network.UpdateDownloadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UpdateUtils {
    private var hasCheckedUpdate = false
    private const val TAG = "UpdateUtils"

    suspend fun checkUpdate(context: Context) {
        if (hasCheckedUpdate) return
        kotlin.runCatching {
            val manager = DownloadManager.getInstance(context)
            withContext(Dispatchers.IO) {
                val versionCode = ApplicationUtil.getAppVersionCode(FunnyApplication.ctx)
                Log.d(TAG, "checkUpdate: VersionCode:$versionCode")
                val channel = DataSaverUtils.readData(Consts.KEY_APP_CHANNEL, "stable")
                val updateInfo = TransNetwork.appUpdateService.getUpdateInfo(versionCode, channel)
                Log.i(TAG, "checkUpdate: $updateInfo")
                if (updateInfo.should_update) {
                    val configuration = UpdateConfiguration().apply {
                        httpManager =
                            UpdateDownloadManager(FunnyApplication.ctx.externalCache.absolutePath)
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
                        .setApkSize(StringUtil.convertIntToSize(updateInfo.apk_size!!))
                    withContext(Dispatchers.Main) {
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