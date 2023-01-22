package com.funny.translation.translate

import android.content.res.Resources
import com.funny.data_saver.core.DataSaverConverter.registerTypeConverters
import com.funny.translation.BaseApplication
import com.funny.translation.bean.UserBean
import com.funny.translation.codeeditor.ui.editor.EditorSchemes
import com.funny.translation.helper.JsonX
import com.funny.translation.sign.SignUtils
import com.funny.translation.translate.ui.thanks.SponsorSortType
import com.funny.translation.translate.utils.FunnyUncaughtExceptionHandler
import com.funny.translation.translate.utils.SortResultUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
            save = { JsonX.toJson(it) },
            restore = { JsonX.fromJson(it, UserBean::class) }
        )

        registerTypeConverters<EditorSchemes>(
            save = { it.name },
            restore = { EditorSchemes.valueOf(it) }
        )

        registerTypeConverters<SponsorSortType>(
            save = { it.name },
            restore = { SponsorSortType.valueOf(it) }
        )

        registerTypeConverters<Pair<String, Int>>(
            save = { "${it.first}:${it.second}" },
            restore = { val split = it.split(":"); Pair(split[0], split[1].toInt()) }
        )

        registerTypeConverters<Language>(
            save = { it.name },
            restore = { Language.valueOf(it) }
        )
    }

    companion object {
        var ctx by Delegates.notNull<FunnyApplication>()
        val resources: Resources get() = ctx.resources
        const val TAG = "FunnyApplication"
    }
}

val appCtx = FunnyApplication.ctx