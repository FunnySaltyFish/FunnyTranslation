package com.funny.translation

import android.content.Context
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.funny.translation.helper.LocaleUtils
import com.smarx.notchlib.NotchScreenManager

open class BaseActivity : AppCompatActivity() {
    private lateinit var callback: OnBackPressedCallback

    companion object {
        private const val TAG = "BaseActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 状态栏沉浸
        NotchScreenManager.getInstance().setDisplayInNotch(this)

        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

    }

    override fun attachBaseContext(newBase: Context?) {
        val context = newBase?.let {
            LocaleUtils.getWarpedContext(it)
        }
        super.attachBaseContext(context)
    }
}