package com.funny.trans.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import com.funny.data_saver.core.LocalDataSaver
import com.funny.trans.login.ui.LoginNavigation
import com.funny.translation.AppConfig
import com.funny.translation.BaseActivity
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.biomertic.BiometricUtils
import com.funny.translation.theme.TransTheme

class LoginActivity : BaseActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BiometricUtils.init()
        }

        setContent {
            CompositionLocalProvider(LocalDataSaver provides DataSaverUtils) {
                TransTheme {
                    LoginNavigation(onLoginSuccess = {
                        Log.d(TAG, "登录成功: 用户: $it")
                        if(it.isValid()) AppConfig.login(it, updateVipFeatures = true)
                        setResult(RESULT_OK, Intent())
                        finish()
                    })
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}