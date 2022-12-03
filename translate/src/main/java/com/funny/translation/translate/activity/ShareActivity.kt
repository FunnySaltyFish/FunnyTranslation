package com.funny.translation.translate.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.funny.translation.BaseApplication
import com.funny.translation.translate.Language
import com.funny.translation.translate.TransActivity
import com.funny.translation.translate.utils.EasyFloatUtils

/**
 * 接收外部分享过来的文本、图片
 */
class ShareActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        EasyFloatUtils.initScreenSize(this)
        EasyFloatUtils.showFloatBall(this)

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
         if (Intent.ACTION_PROCESS_TEXT == intent.action && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val text = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)?.trim() ?: ""
            if (text != "") {
                Log.d(TransActivity.TAG, "获取到输入法菜单传来的文本: $text")
                EasyFloatUtils.showTransWindow()
                EasyFloatUtils.startTranslate(text, Language.AUTO, Language.CHINESE)
                if (!TransActivity.initialized){
                    startActivity(Intent(this, TransActivity::class.java))
                }
                finish()
            }
        }
    }
}