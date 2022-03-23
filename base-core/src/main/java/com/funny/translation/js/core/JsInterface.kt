package com.funny.translation.js.core

import androidx.annotation.Keep
import com.funny.translation.debug.Debug
import com.funny.translation.js.JsManager
import com.funny.translation.js.extentions.show
import com.funny.translation.network.OkHttpUtils
import org.mozilla.javascript.NativeArray
import org.mozilla.javascript.NativeObject

@Keep
interface JsInterface {
    fun get(
        url : String,
        headersMap : HashMap<String,String>? = null
    ) = OkHttpUtils.get(url = url,headersMap = headersMap)

    fun getRaw(
        url : String,
        headersMap : HashMap<String,String>? = null
    ) = OkHttpUtils.getRaw(url = url,headersMap = headersMap)

    fun getResponse(
        url : String,
        headersMap : HashMap<String,String>? = null
    ) = OkHttpUtils.getResponse(url, headersMap)

    fun post(
        url : String,
        json : String,
        headersMap : HashMap<String,String>? = null
    ) = OkHttpUtils.postJSON(url, json, headersMap)

    fun post(
        url : String,
        data : HashMap<String,String>,
        headersMap : HashMap<String,String>? = null
    ) = OkHttpUtils.postForm(url, data, headersMap)

    fun getOkHttpClient() = OkHttpUtils.okHttpClient

    fun log(obj : Any){
        val logStr = when(obj){
            is NativeArray -> obj.show
            is NativeObject -> obj.show()
            else -> obj.toString()
        }
        Debug.log(
            logStr,
            tempSource = "log",
            JsManager.currentRunningJsEngine?.jsBean?.debugMode?:false
        )
    }
}