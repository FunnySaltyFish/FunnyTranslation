package com.funny.translation.translate

import android.content.res.Resources
import android.util.Log
import android.view.WindowManager
import com.funny.translation.BaseApplication
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.utils.FloatWindowUtils
import kotlin.properties.Delegates

class FunnyApplication : BaseApplication() {

    override fun onCreate() {
        super.onCreate()
        ctx = this
    }

    companion object {
        var ctx by Delegates.notNull<FunnyApplication>()
        val resources: Resources get() = ctx.resources
        const val TAG = "FunnyApplication"
    }
}