package com.funny.translation.translate

import android.content.res.Resources
import com.funny.data_saver.core.registerTypeConverters
import com.funny.translation.BaseApplication
import com.funny.translation.bean.UserBean
import com.funny.translation.translate.utils.FunnyUncaughtExceptionHandler
import com.funny.translation.translate.utils.localDataGson
import kotlin.properties.Delegates

class FunnyApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        ctx = this

        registerTypeConverters(
            save = { localDataGson.toJson(it) },
            restore = { localDataGson.fromJson(it, UserBean::class.java) as UserBean }
        )

        FunnyUncaughtExceptionHandler.getInstance().init(ctx)
    }

    companion object {
        var ctx by Delegates.notNull<FunnyApplication>()
        val resources: Resources get() = ctx.resources
        const val TAG = "FunnyApplication"

    }
}

val appCtx = FunnyApplication.ctx