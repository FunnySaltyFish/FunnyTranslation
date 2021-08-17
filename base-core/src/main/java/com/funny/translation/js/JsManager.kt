package com.funny.translation.js

object JsManager {
    private val jsMap : HashMap<Int,JsEngine> = HashMap()
    var currentRunningJsEngine : JsEngine? = null

    fun getJsEngineById(id : Int) : JsEngine? = jsMap[id]
    fun addJSEngine(jsEngine: JsEngine){
        jsMap[jsEngine.getId()] = jsEngine
    }

    fun clear(){
        jsMap.clear()
    }
}