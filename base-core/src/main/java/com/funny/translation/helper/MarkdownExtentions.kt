package com.funny.translation.helper

import android.content.Context
import io.noties.markwon.Markwon
import io.noties.markwon.SoftBreakAddsNewLinePlugin

object MarkdownUtils{
    fun getDefaultMarkwon(context : Context) : Markwon {
        return Markwon.builder(context)
            .usePlugin(SoftBreakAddsNewLinePlugin.create())
            .build()
    }
}

