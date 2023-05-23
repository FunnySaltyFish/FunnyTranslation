package com.funny.translation.translate

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavHostController
import com.azhon.appupdate.utils.ApkUtil
import com.funny.translation.AppConfig
import com.funny.translation.Consts
import com.funny.translation.debug.Debug
import com.funny.translation.debug.DefaultDebugTarget
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.externalCache
import com.funny.translation.network.NetworkReceiver
import com.funny.translation.translate.utils.EasyFloatUtils
import com.smarx.notchlib.NotchScreenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 你好，很高兴见到你！
 *
 * 考虑到最近项目的star逐渐加多，目前（2022年7月13日） 已经有近50个了，所以感觉上我应该更负责一些。
 * 所以在这里，我尝试添加了一些注释，希望对阅读有所帮助。有任何建议或意见欢迎 [issue](https://github.com/FunnySaltyFish/FunnyTranslation/issues) 或 [PR](https://github.com/FunnySaltyFish/FunnyTranslation/pulls)！
 *
 * 我也建议您在充分体验项目功能后再来阅读代码，这会很有帮助的
 *
 * [TransActivity] 是项目的主 Activity，大多数你见到的页面都是这个 Activity 下的。
 * 这是项目的入口页面，一些初始化工作也由它来完成。翻译页面见 [MainScreen()]
 *
 */

class TransActivity : AppCompatActivity() {
    private lateinit var activityViewModel: ActivityViewModel
    private lateinit var context: Context
    private lateinit var netWorkReceiver: NetworkReceiver
    // 保存 NavController，不太优雅，但是为了跳转方便，先这样吧
    internal var navController: NavHostController? = null

    companion object {
        const val TAG = "TransActivity"
        var initialized = false
    }

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Debug.addTarget(DefaultDebugTarget)

        context = this
        activityViewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)

        registerNetworkReceiver()
        getIntentData(intent)

        // 状态栏沉浸
        WindowCompat.setDecorFitsSystemWindows(window, false)
        NotchScreenManager.getInstance().setDisplayInNotch(this)

        setContent {
            // 此处通过这种方式传递 Activity 级别的 ViewModel，以确保获取到的都是同一个实例
            CompositionLocalProvider(LocalActivityVM provides activityViewModel) {
                AppNavigation {
                    this.finish()
                }
            }
        }

        if (!initialized) {
            // 做一些耗时的后台任务
            lifecycleScope.launch(Dispatchers.IO) {
                // MobileAds.initialize(context) {}
                activityViewModel.getNotice()
                activityViewModel.checkUpdate(context)
                ApkUtil.deleteOldApk(context, FunnyApplication.ctx.externalCache.absolutePath + "/update_apk.apk")
            }

            // 显示悬浮窗
            EasyFloatUtils.initScreenSize()
            val showFloatWindow = AppConfig.sShowFloatWindow.value
            if (showFloatWindow) {
                EasyFloatUtils.showFloatBall(this)
            }
            initialized = true
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "onConfigurationChanged: ")
        EasyFloatUtils.resetFloatBallPlace()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        getIntentData(intent)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: ")
        EasyFloatUtils.dismissAll()
        unregisterReceiver(netWorkReceiver)
        DataSaverUtils.remove(Consts.KEY_APP_CURRENT_SCREEN)
        super.onDestroy()
    }

    /**
     * 注册用于网络监听的广播，以判断网络状况
     */
    private fun registerNetworkReceiver() {
        netWorkReceiver = NetworkReceiver()
        IntentFilter().apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        }.let {
            registerReceiver(netWorkReceiver, it)
        }

        // 初始化一下最开始的网络状态
        NetworkReceiver.setNetworkState(context)
    }

    /**
     * 处理从各种地方传过来的 intent
     * @param intent Intent?
     */
    private fun getIntentData(intent: Intent?) {
        val action: String? = intent?.action
        // 这里处理以 url 形式传递的文本
//        if (Intent.ACTION_VIEW == action) {
//            val data: Uri? = intent.data
//            // Log.d(TAG, "getIntentData: data:$data")
//            if (data != null && data.scheme == "funny" && data.host == "translation") {
//                with(activityViewModel.tempTransConfig) {
//                    sourceString = data.getQueryParameter("text")
//                    val s = data.getQueryParameter("sourceId")
//                    if (s != null) sourceLanguage = findLanguageById(s.toInt())
//                    val t = data.getQueryParameter("targetId")
//                    if (t != null) targetLanguage = findLanguageById(t.toInt())
//                }
//                Log.d(TAG, "getIntentData: ${activityViewModel.tempTransConfig}")
//                // shouldJumpToMainScreen.value = true
//            }
//        }
//        // 这里处理的是外部分享过来的文本
//        else
        if (Intent.ACTION_SEND == action && "text/plain" == intent.type) {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim() ?: ""
            if (text != "") {
                Log.d(TAG, "获取到其他应用传来的文本: $text")
                navigateToTextTrans(text, Language.AUTO, Language.CHINESE)
            }
        }
        // 这里是处理输入法选中后的菜单
        else if (Intent.ACTION_PROCESS_TEXT == action && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val text = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)?.trim() ?: ""
            if (text != "") {
                Log.d(TAG, "获取到输入法菜单传来的文本: $text")
                navigateToTextTrans(text, Language.AUTO, Language.CHINESE)
                // shouldJumpToMainScreen.value = true
            }
        }
        // 图片
        // "funny://translation/image_translate?imageUri={imageUri}&sourceId={sourceId}&targetId={targetId}"
         else if (intent?.data?.scheme == "funny" && intent.data?.host == "translation") {
            val data = intent.data
            if (data != null) {
                // 图片翻译，手动跳转
                if (data.path == "/image_translate") {
                    navController?.navigate(data)
                }
            }
        }
    }

    private fun navigateToTextTrans(sourceText: String, sourceLanguage: Language, targetLanguage: Language) {
        navController?.navigate(
            NavDeepLinkRequest.Builder
                .fromUri(Uri.parse("funny://translation/translate?text=$sourceText&sourceId=${sourceLanguage.id}&targetId=${targetLanguage.id}"))
                .build()
        )
    }
}