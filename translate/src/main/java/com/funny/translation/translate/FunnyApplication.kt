package com.funny.translation.translate

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import com.funny.translation.BaseApplication
import com.funny.translation.translate.utils.FunnyUncaughtExceptionHandler
import kotlin.properties.Delegates
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

class FunnyApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        ctx = this
        FunnyUncaughtExceptionHandler.getInstance().init(ctx)
    }

    companion object {
        var ctx by Delegates.notNull<FunnyApplication>()
        val resources: Resources get() = ctx.resources
        const val TAG = "FunnyApplication"


    }
}

val appCtx = FunnyApplication.ctx