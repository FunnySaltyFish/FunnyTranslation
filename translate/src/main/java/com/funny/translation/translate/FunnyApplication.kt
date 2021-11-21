package com.funny.translation.translate

import android.content.res.Resources
import com.funny.translation.BaseApplication
import com.funny.translation.helper.DataStoreUtils
import kotlin.properties.Delegates

class FunnyApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        ctx = this
        DataStoreUtils.init(this)
    }

    companion object {
        var ctx by Delegates.notNull<FunnyApplication>()
        val resources: Resources get() = ctx.resources
    }
}