package com.funny.translation.network

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object OkHttpUtils {
    private val okHttpClient by lazy {
        OkHttpClient
            .Builder()
            .connectTimeout(10,TimeUnit.SECONDS)
            .readTimeout(15,TimeUnit.SECONDS)
            .build()
    }

    fun get(
        url : String,
        headersMap : HashMap<String,String>? = null
    ) : String{
        val requestBuilder = Request.Builder().url(url).get()
        headersMap?.let {
            requestBuilder.addHeaders(it)
        }
        val response = okHttpClient.newCall(requestBuilder.build()).execute()
        return response.body?.string() ?: ""
    }

    fun getRaw(
        url : String,
        headersMap: HashMap<String, String>? = null
    ) : ByteArray {
        val requestBuilder = Request.Builder().url(url).get()
        headersMap?.let {
            requestBuilder.addHeaders(it)
        }
        val response = okHttpClient.newCall(requestBuilder.build()).execute()
        return response.body?.bytes() ?: ByteArray(0)
    }

    fun Request.Builder.addHeaders(headers: Map<String, String>) {
        headers.forEach {
            addHeader(it.key, it.value)
        }
    }
}