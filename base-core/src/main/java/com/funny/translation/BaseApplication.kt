package com.funny.translation

import android.app.Application
import android.content.Context
import android.content.res.Resources
import kotlin.properties.Delegates


open class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ctx = this
    }

    companion object {
        var ctx : Application by Delegates.notNull()
        val resources: Resources get() = ctx.resources
    }
}