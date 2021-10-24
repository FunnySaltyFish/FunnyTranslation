package com.funny.translation.translate

import android.app.Application
import android.content.res.Resources
import com.funny.translation.helper.DataStoreUtils
import com.funny.translation.helper.dataStore
import kotlin.properties.Delegates

class FunnyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ctx = this

        DataStoreUtils.init(this)
    }

    companion object {
        var ctx : FunnyApplication by Delegates.notNull()
        val resources: Resources get() = ctx.resources
    }
}