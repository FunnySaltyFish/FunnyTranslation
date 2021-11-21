package com.funny.translation.translate

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.ViewModel
import com.azhon.appupdate.config.UpdateConfiguration
import com.azhon.appupdate.manager.DownloadManager
import com.azhon.appupdate.utils.ApkUtil
import com.azhon.appupdate.utils.ApkUtil.*
import com.funny.translation.helper.DataStoreUtils
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.network.TransNetwork
import com.funny.translation.translate.utils.ApplicationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

import java.io.File
import java.lang.Exception


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

//    suspend fun checkUpdate(context : Context){
//        if(hasCheckedUpdate)return
//        withContext(Dispatchers.IO){
//            val versionCode = ApplicationUtil.getAppVersionCode(FunnyApplication.ctx)
//            val channel = DataStoreUtils.getSyncData(Consts.KEY_APP_CHANNEL, "stable")
//            val updateInfo = TransNetwork.appUpdateService.getUpdateInfo(versionCode, channel)
//            Log.i(TAG, "checkUpdate: $updateInfo")
//            if (updateInfo.should_update){
//                val appUpdate = AppUpdate.Builder()
//                        //更新地址（必传）
//                        .newVersionUrl(updateInfo.apk_url)
//                        // 版本号（非必填）
//                        .newVersionCode(updateInfo.version_name)
//                        // 通过传入资源id来自定义更新对话框，注意取消更新的id要定义为btnUpdateLater，立即更新的id要定义为btnUpdateNow（非必填）
//                        .updateResourceId(R.layout.dialog_update)
//                        // 更新的标题，弹框的标题（非必填，默认为应用更新）
//                        .updateTitle(R.string.update_title)
//                        // 更新内容的提示语，内容的标题（非必填，默认为更新内容）
//                        .updateContentTitle(R.string.update_content_lb)
//                        // 更新内容（非必填，默认“1.用户体验优化\n2.部分问题修复”）
//                        .updateInfo(updateInfo.update_log)
//                        // 文件大小（非必填）
//                        .fileSize(updateInfo.apk_size!!.toSize)
//                        //是否采取静默下载模式（非必填，只显示更新提示，后台下载完自动弹出安装界面），否则，显示下载进度，显示下载失败
//                        .isSilentMode(false)
//                        //是否强制更新（非必填，默认不采取强制更新，否则，不更新无法使用）
//                        .forceUpdate(if(updateInfo.force_update == true) 1 else 0) //文件的MD5值，默认不传，如果不传，不会去验证md5(非静默下载模式生效，若有值，且验证不一致，会启动浏览器去下载)
//                        .md5(updateInfo.apk_md5)
//                        .build()
//                UpdateManager().startUpdate(context, appUpdate)
//            }
//        }
//        hasCheckedUpdate = true
//    }

    suspend fun checkUpdate(context : Context){
        if(hasCheckedUpdate)return
        val manager = DownloadManager.getInstance(context);
        withContext(Dispatchers.IO){
            val versionCode = ApplicationUtil.getAppVersionCode(FunnyApplication.ctx)
            val channel = DataStoreUtils.getSyncData(Consts.KEY_APP_CHANNEL, "stable")
            val updateInfo = TransNetwork.appUpdateService.getUpdateInfo(versionCode, channel)
            Log.i(TAG, "checkUpdate: $updateInfo")
            if (updateInfo.should_update){
                val configuration = UpdateConfiguration().apply {
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
    }
}