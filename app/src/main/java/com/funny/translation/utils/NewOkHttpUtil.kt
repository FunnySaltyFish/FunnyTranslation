package com.funny.translation.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

class NewOkHttpUtil {
    private val okHttpClient : OkHttpClient by lazy{
        OkHttpClient.Builder().readTimeout(15,TimeUnit.SECONDS).connectTimeout(15,TimeUnit.SECONDS).build()
    }

    fun getClient(): OkHttpClient {
        return okHttpClient
    }

    @Throws(IOException::class)
    fun get(url: String): String? {
        val client = getClient()
        val request: Request = Request.Builder()
                .url(url)
                .build()
        val response: Response = client.newCall(request).execute()
        return response.body?.string()
    }
}