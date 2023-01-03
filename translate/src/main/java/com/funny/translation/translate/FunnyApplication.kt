package com.funny.translation.translate

import android.content.Context
import android.content.res.Resources
import android.util.Log
import com.azhon.appupdate.config.UpdateConfiguration
import com.azhon.appupdate.manager.DownloadManager
import com.azhon.appupdate.utils.ApkUtil
import com.funny.data_saver.core.registerTypeConverters
import com.funny.translation.BaseApplication
import com.funny.translation.Consts
import com.funny.translation.bean.UserBean
import com.funny.translation.codeeditor.extensions.externalCache
import com.funny.translation.codeeditor.ui.editor.EditorSchemes
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.sign.SignUtils
import com.funny.translation.translate.network.TransNetwork
import com.funny.translation.translate.network.UpdateDownloadManager
import com.funny.translation.translate.ui.thanks.SponsorSortType
import com.funny.translation.translate.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

class FunnyApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        ctx = this
        FunnyUncaughtExceptionHandler.getInstance().init(ctx)

        GlobalScope.launch {
            initLanguageDisplay(resources)
            SignUtils.loadJs()
            SortResultUtils.init()

        }

        // For ComposeDataSaver
        registerTypeConverters<UserBean>(
            save = { localDataGson.toJson(it) },
            restore = { localDataGson.fromJson(it, UserBean::class.java) as UserBean }
        )

        registerTypeConverters<EditorSchemes>(
            save = { it.name },
            restore = { EditorSchemes.valueOf(it) }
        )

        registerTypeConverters<SponsorSortType>(
            save = { it.name },
            restore = { SponsorSortType.valueOf(it) }
        )
    }

    companion object {
        var ctx by Delegates.notNull<FunnyApplication>()
        val resources: Resources get() = ctx.resources
        const val TAG = "FunnyApplication"
    }


}

val appCtx = FunnyApplication.ctx