package com.funny.translation.translate

import android.app.Application
import android.content.res.Resources
import kotlin.properties.Delegates

class FunnyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ctx = this
    }

    companion object {
        var ctx : FunnyApplication by Delegates.notNull()
        val resources: Resources get() = ctx.resources
    }
}