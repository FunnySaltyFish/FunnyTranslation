package com.funny.translation.translate

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.azhon.appupdate.utils.ApkUtil
import com.funny.translation.codeeditor.extensions.externalCache
import com.funny.translation.debug.Debug
import com.funny.translation.debug.DefaultDebugTarget
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.trans.findLanguageById
import com.funny.translation.trans.initLanguageDisplay
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.utils.EasyFloatUtils
import com.funny.translation.translate.utils.SortResultUtils
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

        context = this
        activityViewModel = ViewModelProvider(this).get(ActivityViewModel::class.java)
        getIntentData(intent)

        lifecycleScope.launch(Dispatchers.IO) {
            initLanguageDisplay(resources)
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        NotchScreenManager.getInstance().setDisplayInNotch(this)

        setContent {
            CompositionLocalProvider(LocalActivityVM provides activityViewModel) {
                AppNavigation(
                    exitAppAction = {
                        this.finish()
                    }
                )
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            SortResultUtils.init()
            activityViewModel.checkUpdate(context)
            ApkUtil.deleteOldApk(
                context,
                context.externalCache.absolutePath + "/update_apk.apk"
            )
        }

        EasyFloatUtils.initScreenSize(this)
        val showFloatWindow = DataSaverUtils.readData(Consts.KEY_SHOW_FLOAT_WINDOW, false)
        if(showFloatWindow){
            EasyFloatUtils.showFloatBall(this)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        getIntentData(intent)
    }

    override fun onDestroy() {
        EasyFloatUtils.dismissAll()
        super.onDestroy()
    }

    /**
     * 处理从各种地方传过来的 intent
     * @param intent Intent?
     */
    private fun getIntentData(intent: Intent?) {
        val action: String? = intent?.getAction()
//        Log.d(TAG, "getIntentData: $intent")
        if (Intent.ACTION_VIEW.equals(action)) {
            val data: Uri? = intent.data
//            Log.d(TAG, "getIntentData: data:$data")
            if (data !=null  && data.scheme == "funny" && data.host == "translation") {
                with(activityViewModel.tempTransConfig){
                    sourceString = data.getQueryParameter("text")
                    val s = data.getQueryParameter("sourceId")
                    if(s!=null) sourceLanguage = findLanguageById(s.toInt())
                    val t = data.getQueryParameter("targetId")
                    if(t!=null) targetLanguage = findLanguageById(t.toInt())
                }
                Log.d(TAG, "getIntentData: ${activityViewModel.tempTransConfig}")
//                Log.d(TAG, "getIntentData: activityVM:${activityViewModel.hashCode()}")
            }
        }
    }
}