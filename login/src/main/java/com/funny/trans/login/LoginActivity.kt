package com.funny.trans.login

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import com.funny.data_saver.core.LocalDataSaver
import com.funny.trans.login.ui.LoginScreen
import com.funny.trans.login.ui.LoginTheme
import com.funny.trans.login.utils.SoterUtils
import com.funny.translation.helper.BiometricUtils
import com.funny.translation.helper.DataSaverUtils
import com.smarx.notchlib.NotchScreenManager

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 状态栏沉浸
        WindowCompat.setDecorFitsSystemWindows(window, false)
        NotchScreenManager.getInstance().setDisplayInNotch(this)

//        SoterUtils.init()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BiometricUtils.init()
        }

        setContent {
            CompositionLocalProvider(LocalDataSaver provides DataSaverUtils) {
                LoginTheme {
                    LoginScreen()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        SoterUtils.destroy()
    }
}