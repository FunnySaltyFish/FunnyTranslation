package com.funny.translation.translate

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.translation.AppConfig
import com.funny.translation.helper.JsonX
import com.funny.translation.helper.UserUtils
import com.funny.translation.network.OkHttpUtils
import com.funny.translation.network.ServiceCreator
import com.funny.translation.network.api
import com.funny.translation.translate.bean.NoticeInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date


class ActivityViewModel : ViewModel(), LifecycleEventObserver {

    var lastBackTime: Long = 0
    var noticeInfo: MutableState<NoticeInfo?> = mutableStateOf(null)

    var userInfo by AppConfig.userInfo
    val uid by derivedStateOf { userInfo.uid }
    val token by derivedStateOf { userInfo.jwt_token }

    // 用于 Composable 跨层级直接观察 Activity 生命周期，实现方法有点奇特
    val activityLifecycleEvent = MutableSharedFlow<Lifecycle.Event>()

    companion object {
        const val TAG = "ActivityVM"
    }

    init {
        refreshUserInfo()
    }

    fun refreshUserInfo() {
        if (userInfo.isValid()) {
            viewModelScope.launch {
                api(UserUtils.userService::getInfo, uid) {
                    success {
                        it.data?.let {  user -> AppConfig.login(user) }
                    }
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

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        viewModelScope.launch {
            // Log.d(TAG, "onStateChanged: emit $event")
            // 等待 Composable 订阅，以避免 Composable 未订阅时发送的事件丢失
            while (activityLifecycleEvent.subscriptionCount.value == 0) {
                delay(100)
            }
            activityLifecycleEvent.emit(event)
        }
    }
}