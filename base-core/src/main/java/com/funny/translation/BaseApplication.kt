package com.funny.translation

import android.app.Activity
import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import com.tencent.mmkv.MMKV
import java.lang.ref.WeakReference
import java.util.*
import kotlin.properties.Delegates


open class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ctx = this
        MMKV.initialize(this)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks{
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {
                if ((activityStack.isNotEmpty() && activityStack.peek() != activity) || activityStack.isEmpty())
                    activityStack.push(activity)
            }

            override fun onActivityStopped(activity: Activity) {
                if (activityStack.isNotEmpty()) activityStack.pop()
            }
        })
    }

    companion object {
        var ctx : Application by Delegates.notNull()
        val resources: Resources get() = ctx.resources

        @Throws(PackageManager.NameNotFoundException::class)
        fun getLocalPackageInfo(): PackageInfo? {
            return ctx.packageManager.getPackageInfo(ctx.packageName, PackageManager.GET_CONFIGURATIONS)
        }

        fun getCurrentActivity() = if (activityStack.isNotEmpty()) activityStack.peek() else null

        private val activityStack = Stack<Activity>()
    }


}