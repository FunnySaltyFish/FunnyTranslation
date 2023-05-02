package com.funny.translation.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

object ScreenUtils {
    private val systemDisplayMetrics by lazy(LazyThreadSafetyMode.NONE) {
        Resources.getSystem().displayMetrics
    }

    fun isStatusBarVisible(view: View): Boolean {
        return ViewCompat.getRootWindowInsets(view)
            ?.isVisible(WindowInsetsCompat.Type.statusBars()) == true
    }

    fun getScreenWidth(): Int {
        return systemDisplayMetrics.widthPixels
    }

    fun getScreenHeight(): Int {
        return systemDisplayMetrics.heightPixels
    }

    fun getScreenDensityDpi(): Int {
        return systemDisplayMetrics.densityDpi
    }


    @SuppressLint("DiscouragedApi", "InternalInsetResource")
    fun getStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            context.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }
}