package com.funny.translation.js.core

import androidx.annotation.Keep
import com.funny.translation.debug.Debug
import com.funny.translation.network.OkHttpUtils

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

    fun print(text : Any){
        Debug.log(text.toString())
    }

    fun log(text : Any){
        Debug.log(text.toString())
    }
}