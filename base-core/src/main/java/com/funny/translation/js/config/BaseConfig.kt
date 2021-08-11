package com.funny.translation.js.config

import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class BaseConfig {
    companion object{
        val SCRIPT_ENGINE: ScriptEngine by lazy {
            ScriptEngineManager().getEngineByName("rhino")
        }
    }
}