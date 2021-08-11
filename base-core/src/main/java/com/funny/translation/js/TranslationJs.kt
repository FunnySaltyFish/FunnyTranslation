package com.funny.translation.js

import com.funny.translation.js.config.BaseConfig.Companion.SCRIPT_ENGINE
import com.funny.translation.js.core.JsInterface
import javax.script.SimpleBindings

class TranslationJs : JsInterface {
    fun eval(code:String){
        val bindings = SimpleBindings().apply {
            this["funny"] = this@TranslationJs
        }
        SCRIPT_ENGINE.eval(code,bindings)
    }
}