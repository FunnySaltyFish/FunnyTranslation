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
import com.funny.translation.codeeditor.extensions.externalCache
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.UserUtils
import com.funny.translation.network.OkHttpUtils
import com.funny.translation.network.ServiceCreator
import com.funny.translation.translate.bean.NoticeInfo
import com.funny.translation.translate.network.TransNetwork
import com.funny.translation.translate.network.UpdateDownloadManager
import com.funny.translation.translate.ui.bean.TranslationConfig
import com.funny.translation.translate.utils.ApplicationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.util.*


class ActivityViewModel : ViewModel() {

    var lastBackTime : Long = 0
    var hasCheckedUpdate = false
    // 由悬浮窗或其他应用传过来的临时翻译参数
    val tempTransConfig = TranslationConfig()
    var noticeInfo : MutableState<NoticeInfo?> = mutableStateOf(null)

    var userInfo by AppConfig.userInfo
    val uid by derivedStateOf { userInfo.uid }
    val token by derivedStateOf { userInfo.jwt_token }

    companion object{
        const val TAG = "ActivityVM"
    }

    init {
        if (userInfo.isValid() && userInfo.email == "") {
            viewModelScope.launch {
                kotlin.runCatching {
                    UserUtils.getUserInfo(userInfo.uid)?.let {
                        userInfo = it
                    }
                }
            }
        }
    }

    suspend fun getNotice(){
        kotlin.runCatching {
            withContext(Dispatchers.IO){
                val jsonBody = OkHttpUtils.get("${ServiceCreator.BASE_URL}/api/notice")
                if (jsonBody != ""){
                    noticeInfo.value = ServiceCreator.gson.fromJson(jsonBody, NoticeInfo::class.java)
                }
            }
        }.onFailure {
            noticeInfo.value = NoticeInfo("获取公告失败，如果网络连接没问题，则服务器可能崩了，请告知开发者修复……", Date(), null)
            it.printStackTrace()
        }
    }

}