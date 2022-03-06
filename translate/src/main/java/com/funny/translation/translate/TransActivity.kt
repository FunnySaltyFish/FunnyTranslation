package com.funny.translation.translate

import android.content.Context
import android.os.Bundle
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
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.utils.EasyFloatUtils
import com.smarx.notchlib.NotchScreenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransActivity : AppCompatActivity() {
    private lateinit var activityViewModel: ActivityViewModel
    lateinit var context: Context

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

        lifecycleScope.launch(Dispatchers.IO) {
            activityViewModel.checkUpdate(context)
            ApkUtil.deleteOldApk(
                context,
                context.externalCache.absolutePath + "/" + "update_apk.apk"
            )
        }

        EasyFloatUtils.initScreenSize(this)
        val showFloatWindow = DataSaverUtils.readData(Consts.KEY_SHOW_FLOAT_WINDOW, false)
        if(showFloatWindow){
            EasyFloatUtils.showFloatBall(this)
        }
    }

    override fun onDestroy() {
        EasyFloatUtils.dismissAll()
        super.onDestroy()
    }
}