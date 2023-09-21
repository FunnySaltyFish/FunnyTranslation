package com.funny.translation.translate.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.funny.translation.AppConfig
import com.funny.translation.Consts
import com.funny.translation.translate.TransActivity
import com.funny.translation.translate.TransActivityIntent
import com.funny.translation.translate.utils.EasyFloatUtils

/**
 * 接收外部分享过来的文本、图片
 * 或者磁贴打开悬浮窗
 */
class ShareActivity : AppCompatActivity() {
    private val shouldOpenFloatingWindow = AppConfig.sTextMenuFloatingWindow.value

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        getIntentData(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            getIntentData(it)
        }
    }

    private fun getIntentData(intent: Intent) {
        // 这里是处理输入法选中后的菜单
        if (
            (Intent.ACTION_PROCESS_TEXT == intent.action && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        ) {
            val text = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)?.trim() ?: ""
            Log.d(TransActivity.TAG, "获取到输入法菜单传来的文本: $text")
            openFloatWindowAndTransActivity(text, shouldOpenFloatingWindow)
        }
        // 这里处理的是外部分享过来的文本
        else if (Intent.ACTION_SEND == intent.action && "text/plain" == intent.type) {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim() ?: ""
            if (text != "") {
                Log.d(TransActivity.TAG, "获取到其他应用传来的文本: $text")
                openFloatWindowAndTransActivity(text, shouldOpenFloatingWindow)
            }
        }
        // 磁贴 -》 译站悬浮窗
        else if (intent.action == Consts.INTENT_ACTION_CLICK_FLOAT_WINDOW_TILE) {
            val openTransWindow =
                intent.getBooleanExtra(Consts.INTENT_EXTRA_OPEN_FLOAT_WINDOW, false)
            if (openTransWindow) {
                TransActivityIntent.OpenFloatWindow.asIntent().let {
                    startActivity(it)
                }
            }
        }
        // 接收到外部分享的图片
        else if (Intent.ACTION_SEND == intent.action && intent.type?.startsWith("image/") == true) {
            val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            Log.d(TAG, "获取到分享的图片: $uri")
            uri ?: return
            // funny://translation/image_translate?imageUri={imageUri}&sourceId={sourceId}&targetId={targetId}
            TransActivityIntent.TranslateImage(uri).asIntent().let {
                startActivity(it)
            }
        }
        finish()
    }

    private fun openFloatWindowAndTransActivity(text: String, openTransWindow: Boolean) {
        if (text.isBlank()) return
        val source = AppConfig.sDefaultSourceLanguage.value
        val target = AppConfig.sDefaultTargetLanguage.value
        // 如果已经开启了悬浮窗，那么就直接翻译
        if (openTransWindow && EasyFloatUtils.isShowingFloatBall()) {
            EasyFloatUtils.showTransWindow()
            EasyFloatUtils.startTranslate(text, source, target)
        } else {
            val intent = TransActivityIntent.TranslateText(
                text = text,
                sourceLanguage = AppConfig.sDefaultSourceLanguage.value,
                targetLanguage = AppConfig.sDefaultTargetLanguage.value,
                byFloatWindow = openTransWindow
            ).asIntent()
            startActivity(intent)
        }
    }
}

private const val TAG = "ShareActivity"