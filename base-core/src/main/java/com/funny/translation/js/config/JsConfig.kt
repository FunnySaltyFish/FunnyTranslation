package com.funny.translation.js.config

import java.lang.StringBuilder
import javax.script.Compilable
import javax.script.Invocable
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class JsConfig {
    companion object{
        val SCRIPT_ENGINE: ScriptEngine by lazy {
            ScriptEngineManager().getEngineByName("rhino")
        }
        val COMPILABLE : Compilable
            get() = SCRIPT_ENGINE as Compilable
        val INVOCABLE
            get() = SCRIPT_ENGINE as Invocable

        const val JS_ENGINE_KIND = 9999.toShort()

        val DEBUG_DIVIDER = "=" * 18
        const val JS_ENGINE_VERSION = 2
    }
}

private operator fun String.times(times: Int): String {
    val stringBuilder = StringBuilder()
    for(i in 0 until times){
        stringBuilder.append(this)
    }
    return stringBuilder.toString()
}
