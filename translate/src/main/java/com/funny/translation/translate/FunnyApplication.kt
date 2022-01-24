package com.funny.translation.translate

import android.content.res.Resources
import com.funny.jetsetting.core.DataSavePreferences
import com.funny.translation.BaseApplication
import kotlin.properties.Delegates

class FunnyApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        ctx = this
        DataSavePreferences.setContext(this)
    }

    companion object {
        var ctx by Delegates.notNull<FunnyApplication>()
        val resources: Resources get() = ctx.resources
    }
}