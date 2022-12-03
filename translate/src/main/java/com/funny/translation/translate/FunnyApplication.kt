package com.funny.translation.translate

import android.content.Context
import android.content.res.Resources
import android.util.Log
import com.azhon.appupdate.config.UpdateConfiguration
import com.azhon.appupdate.manager.DownloadManager
import com.azhon.appupdate.utils.ApkUtil
import com.funny.data_saver.core.registerTypeConverters
import com.funny.translation.BaseApplication
import com.funny.translation.Consts
import com.funny.translation.bean.UserBean
import com.funny.translation.codeeditor.extensions.externalCache
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.sign.SignUtils
import com.funny.translation.translate.network.TransNetwork
import com.funny.translation.translate.network.UpdateDownloadManager
import com.funny.translation.translate.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

class FunnyApplication : BaseApplication() {
    private var hasCheckedUpdate = false
    override fun onCreate() {
        super.onCreate()
        ctx = this
        FunnyUncaughtExceptionHandler.getInstance().init(ctx)

        GlobalScope.launch {
            initLanguageDisplay(resources)
            SignUtils.loadJs()
            SortResultUtils.init()
            checkUpdate(ctx)
            ApkUtil.deleteOldApk(ctx, ctx.externalCache.absolutePath + "/update_apk.apk")
        }

        // For ComposeDataSaver
        registerTypeConverters(
            save = { localDataGson.toJson(it) },
            restore = { localDataGson.fromJson(it, UserBean::class.java) as UserBean }
        )
    }

    companion object {
        var ctx by Delegates.notNull<FunnyApplication>()
        val resources: Resources get() = ctx.resources
        const val TAG = "FunnyApplication"
    }

    suspend fun checkUpdate(context : Context){
        if(hasCheckedUpdate) return
        kotlin.runCatching {
            val manager = DownloadManager.getInstance(context)
            withContext(Dispatchers.IO){
                val versionCode = ApplicationUtil.getAppVersionCode(ctx)
                Log.d(ActivityViewModel.TAG, "checkUpdate: VersionCode:$versionCode")
                val channel = DataSaverUtils.readData(Consts.KEY_APP_CHANNEL, "stable")
                val updateInfo = TransNetwork.appUpdateService.getUpdateInfo(versionCode, channel)
                Log.i(ActivityViewModel.TAG, "checkUpdate: $updateInfo")
                if (updateInfo.should_update){
                    val configuration = UpdateConfiguration().apply {
                        httpManager = UpdateDownloadManager(ctx.externalCache.absolutePath)
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

val appCtx = FunnyApplication.ctx