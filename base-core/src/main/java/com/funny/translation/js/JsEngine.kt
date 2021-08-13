package com.funny.translation.js

import com.funny.translation.debug.Debug
import com.funny.translation.js.bean.JsBean
import com.funny.translation.js.config.BaseConfig
import com.funny.translation.js.config.BaseConfig.Companion.COMPILABLE
import com.funny.translation.js.core.JsInterface
import javax.script.ScriptException
import javax.script.SimpleBindings

class JsEngine(val jsBean: JsBean) : JsInterface {
    private val compiled by lazy{ COMPILABLE.compile(jsBean.code) }
    fun eval(bindings: SimpleBindings){
        compiled.eval(bindings)
    }

    @Throws(ScriptException::class,NoSuchMethodException::class)
    fun evalFunction(name : String,vararg arguments : Any) : Any? {
        return BaseConfig.INVOCABLE.invokeFunction(name,arguments)
    }

    fun getProperty(name : String) : Any? {
        return BaseConfig.SCRIPT_ENGINE.get(name)
    }

    fun getId() = jsBean.id

    val isOffline
        get() = evalFunction("isOffline") as Boolean

    fun loadBasicConfigurations(){
        val bindings = SimpleBindings().apply {
            this["java"] = this@JsEngine
        }
        eval(bindings)

        with(jsBean){
            val funnyJs = getProperty("FunnyJS")
            Debug.log(funnyJs.toString())
        }
    }


}