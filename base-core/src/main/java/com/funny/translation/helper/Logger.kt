package com.funny.translation.helper

import android.util.Log as AndroidLog

object Log {
    // i, d, w, e, v
    fun d(tag: String, msg: String) = AndroidLog.d(tag, msg)
    fun d(tag: String, msg: String, tr: Throwable) = AndroidLog.d(tag, msg, tr)
    
    fun i(tag: String, msg: String) = AndroidLog.i(tag, msg)
    fun i(tag: String, msg: String, tr: Throwable) = AndroidLog.i(tag, msg, tr)
    
    fun w(tag: String, msg: String) = AndroidLog.w(tag, msg)
    fun w(tag: String, msg: String, tr: Throwable) = AndroidLog.w(tag, msg, tr)
    
    fun e(tag: String, msg: String) = AndroidLog.e(tag, msg)
    fun e(tag: String, msg: String, tr: Throwable) = AndroidLog.e(tag, msg, tr)
    
    fun v(tag: String, msg: String) = AndroidLog.v(tag, msg)
    fun v(tag: String, msg: String, tr: Throwable) = AndroidLog.v(tag, msg, tr)
}