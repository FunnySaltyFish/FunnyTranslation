package com.funny.translation.translate.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.funny.translation.AppConfig
import com.funny.translation.Consts
import com.funny.translation.translate.Language
import com.funny.translation.translate.TransActivity
import com.funny.translation.translate.utils.EasyFloatUtils

/**
 * 接收外部分享过来的文本、图片
 * 或者磁贴打开悬浮窗
 */
class ShareActivity: AppCompatActivity() {
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

    private fun getIntentData(intent: Intent){
        // 这里是处理输入法选中后的菜单
         if (
             (Intent.ACTION_PROCESS_TEXT == intent.action && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
         ) {
            val text = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)?.trim() ?: ""
            Log.d(TransActivity.TAG, "获取到输入法菜单传来的文本: $text")
            openFloatWindowAndTransActivity(text, shouldOpenFloatingWindow)
        }
        // 磁贴 -》 译站悬浮窗
        else if(intent.action == Consts.INTENT_ACTION_CLICK_FLOAT_WINDOW_TILE){
            val openTransWindow = intent.getBooleanExtra(Consts.INTENT_EXTRA_OPEN_FLOAT_WINDOW, false)
            if (openTransWindow) openFloatWindowAndTransActivity("", true)
        }
        // 接收到外部分享的图片
        else if (Intent.ACTION_SEND == intent.action && intent.type?.startsWith("image/") == true) {
            val uri = intent.getParcelableExtra<android.net.Uri>(Intent.EXTRA_STREAM)
            Log.d(TransActivity.TAG, "获取到分享的图片: $uri")
            uri ?: return
            // funny://translation/image_translate?imageUri={imageUri}&sourceId={sourceId}&targetId={targetId}
            startActivity(Intent(this, TransActivity::class.java).apply {
                data = android.net.Uri.parse("funny://translation/image_translate?imageUri=$uri")
            })
        }

        finish()
    }

    private fun openFloatWindowAndTransActivity(text: String, openTransWindow: Boolean){
        if (openTransWindow) {
            EasyFloatUtils.initScreenSize()
            EasyFloatUtils.showFloatBall(this)
            EasyFloatUtils.showTransWindow()
            if (text != "") EasyFloatUtils.startTranslate(text, Language.AUTO, Language.CHINESE)
            if (!TransActivity.initialized) {
                startActivity(Intent(this, TransActivity::class.java))
            }
        } else {
            intent.setClass(this, TransActivity::class.java)
            startActivity(intent)
        }
    }
}