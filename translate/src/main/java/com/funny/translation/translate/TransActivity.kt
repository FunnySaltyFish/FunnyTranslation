package com.funny.translation.translate

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.azhon.appupdate.utils.ApkUtil
import com.funny.translation.codeeditor.extensions.externalCache
import com.funny.translation.debug.Debug
import com.funny.translation.debug.DefaultDebugTarget
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.trans.initLanguageDisplay
import com.funny.translation.translate.bean.AppConfig
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.utils.FloatWindowUtils
import com.smarx.notchlib.NotchScreenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransActivity : AppCompatActivity() {
    private lateinit var activityViewModel: ActivityViewModel
    lateinit var context: Context
    private lateinit var clipboardManager: ClipboardManager
    private lateinit var onPrimaryClipChangedListener: ClipboardManager.OnPrimaryClipChangedListener

    companion object {
        const val TAG = "TransActivity"
    }

    @OptIn(
        ExperimentalComposeUiApi::class,
        ExperimentalMaterialApi::class,
        ExperimentalAnimationApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Debug.addTarget(DefaultDebugTarget)

        //WindowCompat.setDecorFitsSystemWindows(window, false)
        context = this
        activityViewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)
        lifecycleScope.launch(Dispatchers.IO) {
            initLanguageDisplay(resources)
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        NotchScreenManager.getInstance().setDisplayInNotch(this)

        setContent {
            AppNavigation(
                exitAppAction = {
                    this.finish()
                }
            )
        }

        FloatWindowUtils.initScreenSize(this)
        lifecycleScope.launch(Dispatchers.IO) {
            activityViewModel.checkUpdate(context)
            ApkUtil.deleteOldApk(
                context,
                context.externalCache.absolutePath + "/" + "update_apk.apk"
            )
        }

        val showFloatWindow = DataSaverUtils.readData(Consts.KEY_SHOW_FLOAT_WINDOW, false)
        Log.d(TAG, "onCreate: showFloatWindow: $showFloatWindow")
        if (showFloatWindow && !AppConfig.INIT_FLOATING_WINDOW) {
            FloatWindowUtils.initFloatingWindow(FunnyApplication.ctx)
            FloatWindowUtils.showFloatWindow()
        }
    }

    private fun registerClipboardEvents() {
        clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        onPrimaryClipChangedListener = ClipboardManager.OnPrimaryClipChangedListener {
            if (clipboardManager.hasPrimaryClip() && clipboardManager.primaryClip?.itemCount ?: 0 > 0) {
                val content = clipboardManager.primaryClip?.getItemAt(0)?.text ?: ""
                if (content.isNotBlank()) {
                    Log.d(TAG, "registerClipboardEvents: $content")
                }
            }
        }
        clipboardManager.addPrimaryClipChangedListener(onPrimaryClipChangedListener)
    }

    override fun onDestroy() {
        if (this::onPrimaryClipChangedListener.isInitialized) clipboardManager.removePrimaryClipChangedListener(
            onPrimaryClipChangedListener
        )
        FloatWindowUtils.destroyFloatWindow()
        super.onDestroy()
    }


}