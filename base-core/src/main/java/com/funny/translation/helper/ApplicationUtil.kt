package com.funny.translation.helper

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

object ApplicationUtil {
    /**
     * 获取当前app version code
     */
    fun getAppVersionCode(context: Context): Long {
        var appVersionCode: Long = 0
        try {
            val packageInfo: PackageInfo = context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0)
            appVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode
            }.toLong()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return appVersionCode
    }

    /**
     * 获取当前app version name
     */
    fun getAppVersionName(context: Context): String {
        var appVersionName = ""
        try {
            val packageInfo: PackageInfo = context.applicationContext
                    .packageManager
                    .getPackageInfo(context.getPackageName(), 0)
            appVersionName = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return appVersionName
    }

    fun restartApp(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
        android.os.Process.killProcess(android.os.Process.myPid())
    }

}