package com.funny.translation.translate

import android.app.Application
import android.content.Context
import android.content.res.Resources
import kotlin.properties.Delegates

class FunnyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    companion object {
        private var instance: FunnyApplication by Delegates.notNull()
        val resources: Resources get() = instance.resources
        val ctx : Context get() = instance
    }
}