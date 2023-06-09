package com.funny.translation.translate

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azhon.appupdate.config.UpdateConfiguration
import com.azhon.appupdate.manager.DownloadManager
import com.funny.translation.AppConfig
import com.funny.translation.Consts
import com.funny.translation.helper.*
import com.funny.translation.network.OkHttpUtils
import com.funny.translation.network.ServiceCreator
import com.funny.translation.translate.bean.NoticeInfo
import com.funny.translation.translate.network.TransNetwork
import com.funny.translation.translate.network.UpdateDownloadManager
import com.funny.translation.translate.utils.ApplicationUtil
import com.funny.translation.translate.utils.StringUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class ActivityViewModel : ViewModel() {

    var lastBackTime: Long = 0
    var hasCheckedUpdate = false

    var noticeInfo: MutableState<NoticeInfo?> = mutableStateOf(null)

    var userInfo by AppConfig.userInfo
    val uid by derivedStateOf { userInfo.uid }
    val token by derivedStateOf { userInfo.jwt_token }

    companion object {
        const val TAG = "ActivityVM"
    }

    init {
        refreshUserInfo()
    }

    fun refreshUserInfo() {
        if (userInfo.isValid()) {
            viewModelScope.launch {
                kotlin.runCatching {
                    UserUtils.getUserInfo(userInfo.uid)?.let {
                        AppConfig.login(it)
                    }
                }.onFailure {
                    appCtx.toastOnUi("刷新用户信息失败~")
                }
            }
        }
    }

    suspend fun getNotice() {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val jsonBody = OkHttpUtils.get("${ServiceCreator.BASE_URL}/api/notice")
                if (jsonBody != "") {
                    noticeInfo.value = JsonX.fromJson(json = jsonBody, NoticeInfo::class)
                }
            }.onFailure {
                noticeInfo.value = NoticeInfo("获取公告失败，如果网络连接没问题，则服务器可能崩了，请告知开发者修复……", Date(), null)
                it.printStackTrace()
            }
        }

    }

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