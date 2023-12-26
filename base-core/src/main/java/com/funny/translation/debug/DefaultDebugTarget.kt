package com.funny.translation.debug

import com.funny.translation.helper.Log

object DefaultDebugTarget : Debug.DebugTarget {
    override val source: String
        get() = "DebugLog"

    override fun appendLog(text: CharSequence) {
        Log.d(source, text.toString())
    }
}