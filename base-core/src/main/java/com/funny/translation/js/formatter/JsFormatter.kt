package com.funny.translation.js.formatter

import com.funny.translation.js.formatter.JavascriptFormatter.JavascriptFormatter

object JsFormatter{
    private val javascriptFormatter by lazy{
        JavascriptFormatter()
    }

    fun format(str:String): String = javascriptFormatter.format(str)
}