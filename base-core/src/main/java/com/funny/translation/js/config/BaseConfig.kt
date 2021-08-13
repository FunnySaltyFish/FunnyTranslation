package com.funny.translation.js.config

import javax.script.Compilable
import javax.script.Invocable
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class BaseConfig {
    companion object{
        val SCRIPT_ENGINE: ScriptEngine by lazy {
            ScriptEngineManager().getEngineByName("rhino")
        }
        val COMPILABLE : Compilable
            get() = SCRIPT_ENGINE as Compilable
        val INVOCABLE
            get() = SCRIPT_ENGINE as Invocable

        const val JS_ENGINE_KIND = 9999.toShort()
    }
}