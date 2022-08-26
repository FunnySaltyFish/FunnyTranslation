package com.funny.trans.login

import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import com.funny.data_saver.core.LocalDataSaver
import com.funny.trans.login.ui.LoginScreen
import com.funny.trans.login.ui.LoginTheme
import com.funny.translation.helper.BiometricUtils
import com.funny.translation.helper.DataSaverUtils
import com.smarx.notchlib.NotchScreenManager

class LoginActivity : AppCompatActivity() {
    private lateinit var callback: OnBackPressedCallback
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 状态栏沉浸
        WindowCompat.setDecorFitsSystemWindows(window, false)
        NotchScreenManager.getInstance().setDisplayInNotch(this)

//        SoterUtils.init()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BiometricUtils.init()
        }

        callback = object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)

        setContent {
            CompositionLocalProvider(LocalDataSaver provides DataSaverUtils) {
                LoginTheme {
                    LoginScreen(onLoginSuccess = {

                    })
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}