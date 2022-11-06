package com.funny.translation.debug

import android.util.Log

object DebugUtils {
    private const val TAG = "DebugUtils"

    fun getDebugLog() {
        val process = Runtime.getRuntime().exec("logcat -d | tail -30")
        val bufferedReader = process.inputStream.bufferedReader()
        bufferedReader.use {
            val text = it.readText()
            Log.d(TAG, "getDebugLog: $text")
        }
    }
}