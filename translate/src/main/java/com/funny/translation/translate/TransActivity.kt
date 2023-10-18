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
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.funny.translation.AppConfig
import com.funny.translation.BaseActivity
import com.funny.translation.Consts
import com.funny.translation.debug.Debug
import com.funny.translation.debug.DefaultDebugTarget
import com.funny.translation.network.NetworkReceiver
import com.funny.translation.translate.utils.DeepLinkManager
import com.funny.translation.translate.utils.EasyFloatUtils
import com.funny.translation.translate.utils.UpdateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

class TransActivity : BaseActivity() {
    private lateinit var activityViewModel: ActivityViewModel
    private lateinit var context: Context
    private lateinit var netWorkReceiver: NetworkReceiver

    // 保存 NavController，不太优雅，但是为了跳转方便，先这样吧
    internal var navController: NavHostController? = null

    companion object {
        const val TAG = "TransActivity"
        var initialized = false
    }

    @OptIn(
        ExperimentalComposeUiApi::class,
        ExperimentalAnimationApi::class,
        ExperimentalMaterialApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Debug.addTarget(DefaultDebugTarget)
        initLanguageDisplay(resources)

        context = this
        activityViewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)
        lifecycle.addObserver(activityViewModel)

        registerNetworkReceiver()
        getIntentData(intent)

        setContent {
            // 此处通过这种方式传递 Activity 级别的 ViewModel，以确保获取到的都是同一个实例
            CompositionLocalProvider(LocalActivityVM provides activityViewModel) {
                AppNavigation(
                    navController = rememberNavController().also { this@TransActivity.navController = it },
                    exitAppAction = { this.finish() }
                )
            }
        }


        if (!initialized) {

            // 做一些耗时的后台任务
            lifecycleScope.launch(Dispatchers.IO) {
                // MobileAds.initialize(context) {}
                activityViewModel.getNotice()
                UpdateUtils.checkUpdate(context)
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

    override fun onResume() {
        super.onResume()
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
        lifecycle.removeObserver(activityViewModel)
        unregisterReceiver(netWorkReceiver)
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
        Log.d(TAG, "getIntentData: action:$action, data:${intent?.data}")
        val transActivityIntent = TransActivityIntent.fromIntent(intent)
        // 处理从应用其他地方传过来的 Url
        if (transActivityIntent != null) {
            Log.d(TAG, "getIntentData: parsed transActivityIntent: $transActivityIntent")
            when (transActivityIntent) {
                is TransActivityIntent.TranslateText -> {
                    if (transActivityIntent.byFloatWindow) {
                        EasyFloatUtils.showFloatBall(this)
                        EasyFloatUtils.showTransWindow()
                        EasyFloatUtils.startTranslate(
                            transActivityIntent.text,
                            transActivityIntent.sourceLanguage
                                ?: AppConfig.sDefaultSourceLanguage.value,
                            transActivityIntent.targetLanguage
                                ?: AppConfig.sDefaultTargetLanguage.value
                        )
                    } else {
                        navigateToTextTrans(
                            transActivityIntent.text,
                            transActivityIntent.sourceLanguage,
                            transActivityIntent.targetLanguage
                        )
                    }
                }

                is TransActivityIntent.TranslateImage -> {
                    navController?.navigate(transActivityIntent.deepLinkUri)
                }

                is TransActivityIntent.OpenFloatWindow -> {
                    EasyFloatUtils.showFloatBall(this)
                }
            }
        } else {
            Log.d(TAG, "getIntentData: 走到了 else, intent: $intent")
            // 这里是处理输入法选中后的菜单
            if (Intent.ACTION_PROCESS_TEXT == action && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val text = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)?.trim() ?: ""
                if (text != "") {
                    Log.d(TAG, "获取到输入法菜单传来的文本: $text")
                    navigateToTextTrans(text)
                }
            }
        }
    }

    private fun navigateToTextTrans(
        sourceText: String,
        sourceLanguage: Language? = null,
        targetLanguage: Language? = null
    ) {
        lifecycleScope.launch {
            while (navController == null) {
                delay(50)
            }
            navController?.navigateToTextTrans(
                sourceText,
                sourceLanguage ?: AppConfig.sDefaultSourceLanguage.value,
                targetLanguage ?: AppConfig.sDefaultTargetLanguage.value
            )
        }
    }
}

// 统一处理 intent
sealed class TransActivityIntent() {
    fun asIntent(): Intent {
        return when (this) {
            is TranslateText -> Intent(Intent.ACTION_VIEW).apply {
                data = DeepLinkManager.buildTextTransUri(
                    text,
                    sourceLanguage,
                    targetLanguage,
                    byFloatWindow)
            }

            is TranslateImage -> Intent(Intent.ACTION_VIEW).apply {
                data = deepLinkUri
            }

            is OpenFloatWindow -> Intent().apply {
                action = Consts.INTENT_ACTION_CLICK_FLOAT_WINDOW_TILE
                putExtra(Consts.INTENT_EXTRA_OPEN_FLOAT_WINDOW, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }.apply {
            setClass(appCtx, TransActivity::class.java)
            // 带到前台
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
    }

    data class TranslateText(
        val text: String,
        val sourceLanguage: Language?,
        val targetLanguage: Language?,
        val byFloatWindow: Boolean = false
    ) : TransActivityIntent()

    data class TranslateImage(val deepLinkUri: Uri) : TransActivityIntent()

    object OpenFloatWindow: TransActivityIntent()

    companion object {
        fun fromIntent(intent: Intent?): TransActivityIntent? {
            intent ?: return null

            if (intent.action == Consts.INTENT_ACTION_CLICK_FLOAT_WINDOW_TILE) {
                return if (intent.getBooleanExtra(Consts.INTENT_EXTRA_OPEN_FLOAT_WINDOW, false)) OpenFloatWindow else null
            }

            val data = intent.data
            data ?: return null

            if (data.scheme == "funny" && data.host == "translation") {
                return when (data.path) {
                    DeepLinkManager.TEXT_TRANS_PATH -> kotlin.run {
                        val text = data.getQueryParameter("text") ?: ""
                        val sourceId = data.getQueryParameter("sourceId")
                        val targetId = data.getQueryParameter("targetId")
                        val byFloatWindow =
                            data.getQueryParameter("byFloatWindow")?.toBoolean() ?: false
                        TranslateText(
                            text,
                            Language.fromId(sourceId),
                            Language.fromId(targetId),
                            byFloatWindow
                        )
                    }
                    DeepLinkManager.IMAGE_TRANS_PATH ->  {
                        TranslateImage(data)
                    }
                    else -> null
                }
            }
            return null
        }
    }
}