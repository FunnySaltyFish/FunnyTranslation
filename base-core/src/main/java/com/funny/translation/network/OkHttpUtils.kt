package com.funny.translation.network

import androidx.annotation.Keep
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

@Keep
object OkHttpUtils {
    val okHttpClient by lazy {
        OkHttpClient
            .Builder()
            .connectTimeout(10,TimeUnit.SECONDS)
            .readTimeout(15,TimeUnit.SECONDS)
            .build()
    }

    @JvmOverloads
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

    @JvmOverloads
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

    @JvmOverloads
    fun getResponse(
        url : String,
        headersMap : HashMap<String,String>? = null
    ) : Response{
        val requestBuilder = Request.Builder().url(url).get()
        headersMap?.let {
            requestBuilder.addHeaders(it)
        }
        return okHttpClient.newCall(requestBuilder.build()).execute()
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun postJSON(
        url: String,
        json: String,
        headers: HashMap<String, String>? = null
    ): String {
        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body: RequestBody = json.toRequestBody(JSON)
        val requestBuilder = Request.Builder()
            .url(url)
            .post(body)
        headers?.let {
            requestBuilder.addHeaders(headers)
        }
        val response: Response = okHttpClient.newCall(requestBuilder.build()).execute()
        return response.body?.string() ?: ""
    }

    @JvmOverloads
    fun postForm(
        url: String,
        form : HashMap<String,String>,
        headers: HashMap<String, String>? = null
    ): String {
        val builder = FormBody.Builder()
        for ((key, value) in form) {
            builder.add(key, value)
        }
        val body: RequestBody = builder.build()
        val requestBuilder = Request.Builder()
            .url(url)
            .post(body)
        headers?.let {
            requestBuilder.addHeaders(headers)
        }
        val response: Response = okHttpClient.newCall(requestBuilder.build()).execute()
        return response.body?.string() ?: ""
    }

    private fun Request.Builder.addHeaders(headers: Map<String, String>) {
        headers.forEach {
            addHeader(it.key, it.value)
        }
    }
}