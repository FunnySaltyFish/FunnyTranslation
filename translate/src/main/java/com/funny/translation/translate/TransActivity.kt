package com.funny.translation.translate

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.azhon.appupdate.utils.ApkUtil
import com.funny.data_saver.core.LocalDataSaver
import com.funny.translation.codeeditor.extensions.externalCache
import com.funny.translation.debug.Debug
import com.funny.translation.debug.DefaultDebugTarget
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.MMKVUtils
import com.funny.translation.trans.initLanguageDisplay
import com.funny.translation.translate.bean.AppConfig
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.ui.main.MainScreen
import com.funny.translation.translate.ui.plugin.PluginScreen
import com.funny.translation.translate.ui.screen.TranslateScreen
import com.funny.translation.translate.ui.settings.AboutScreen
import com.funny.translation.translate.ui.settings.SettingsScreen
import com.funny.translation.translate.ui.thanks.ThanksScreen
import com.funny.translation.translate.ui.theme.TransTheme
import com.funny.translation.translate.ui.widget.CustomNavigation
import com.funny.translation.translate.utils.FloatWindowUtils
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransActivity : AppCompatActivity() {
    private lateinit var activityViewModel: ActivityViewModel
    lateinit var context : Context
    private lateinit var clipboardManager : ClipboardManager
    private lateinit var onPrimaryClipChangedListener: ClipboardManager.OnPrimaryClipChangedListener

    companion object {
        const val TAG = "TransActivity"
    }

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Debug.addTarget(DefaultDebugTarget)

        //WindowCompat.setDecorFitsSystemWindows(window, false)
        context = this
        activityViewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)
        lifecycleScope.launch(Dispatchers.IO) {
            initLanguageDisplay(resources)
        }

        setContent {
            CompositionLocalProvider(LocalDataSaver provides MMKVUtils){
                AppNavigation(
                    exitAppAction = {
                        this.finish()
                    }
                )
            }
        }

        FloatWindowUtils.initScreenSize(this)
        lifecycleScope.launch(Dispatchers.IO) {
            activityViewModel.checkUpdate(context)
            ApkUtil.deleteOldApk(context, context.externalCache.absolutePath + "/" + "update_apk.apk")
        }

        val showFloatWindow = DataSaverUtils.readData(Consts.KEY_SHOW_FLOAT_WINDOW,false)
        Log.d(TAG, "onCreate: showFloatWindow: $showFloatWindow")
        if(showFloatWindow && !AppConfig.INIT_FLOATING_WINDOW){
            FloatWindowUtils.initFloatingWindow(FunnyApplication.ctx)
            FloatWindowUtils.showFloatWindow()
        }
    }

    private fun registerClipboardEvents(){
        clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        onPrimaryClipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
            if (clipboardManager.hasPrimaryClip() && clipboardManager.primaryClip?.itemCount?:0 > 0){
                val content = clipboardManager.primaryClip?.getItemAt(0)?.text ?: ""
                if (content.isNotBlank()){
                    Log.d(TAG, "registerClipboardEvents: $content")
                }
            }
        }
        clipboardManager.addPrimaryClipChangedListener(onPrimaryClipChangedListener)
    }

    override fun onDestroy() {
        if(this::onPrimaryClipChangedListener.isInitialized) clipboardManager.removePrimaryClipChangedListener(onPrimaryClipChangedListener)
        FloatWindowUtils.destroyFloatWindow()
        super.onDestroy()
    }


}