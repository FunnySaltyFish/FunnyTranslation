package com.funny.trans.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.funny.data_saver.core.LocalDataSaver
import com.funny.trans.login.ui.GameScreen
import com.funny.trans.login.ui.GameViewModel
import com.funny.trans.login.ui.LoginScreen
import com.funny.trans.login.ui.LoginTheme
import com.funny.translation.helper.BiometricUtils
import com.funny.translation.helper.DataSaverUtils
import com.smarx.notchlib.NotchScreenManager

class GameActivity : AppCompatActivity() {
    private lateinit var callback: OnBackPressedCallback
    private lateinit var vm: GameViewModel

    companion object {
        private const val TAG = "GameActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 状态栏沉浸
        WindowCompat.setDecorFitsSystemWindows(window, false)
        NotchScreenManager.getInstance().setDisplayInNotch(this)

        vm = ViewModelProvider(this).get(GameViewModel::class.java)

        callback = object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)

        setContent {
            CompositionLocalProvider(LocalDataSaver provides DataSaverUtils) {
                LoginTheme {
                    GameScreen(
                        Modifier
                            .fillMaxSize()
                            .statusBarsPadding())
                }
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: vm.password: ${vm.password}, vm.pwdError: ${vm.isPwdError},")
        if(!vm.isPwdError && !vm.isRepeatPwdError){
            setResult(RESULT_OK, Intent().apply {
                putExtra("password", vm.password)
            })
        }
        super.onDestroy()
    }
}