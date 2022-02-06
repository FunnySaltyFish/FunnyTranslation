package com.funny.translation.network

import android.util.Log
import androidx.annotation.Keep
import com.funny.translation.BaseApplication
import com.funny.translation.helper.DataSaverUtils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

@Keep
object OkHttpUtils {
    private const val SAVE_USER_LOGIN_KEY = "user/login"
    private const val SAVE_USER_REGISTER_KEY = "user/register"
    private const val SET_COOKIE_KEY = "set-cookie"
    private const val COOKIE_NAME = "Cookie"
    private const val CONNECT_TIMEOUT = 15L
    private const val READ_TIMEOUT = 10L
    private const val TAG = "OkHttpUtils"

    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
    private fun saveCookie(url: String?, domain: String?, cookies: String) {
        url ?: return
        DataSaverUtils.saveData(url, cookies)
        domain ?: return
        DataSaverUtils.saveData(domain, cookies)
    }

    private val cache = Cache(BaseApplication.ctx.cacheDir, 1024*1024*20L)

    init {
        Log.d(TAG, "cache path: ${BaseApplication.ctx.cacheDir}")
    }

    val okHttpClient by lazy {
        OkHttpClient().newBuilder().apply {
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            cache(cache)
            addInterceptor(HttpCacheInterceptor())
            // get response cookie
            addInterceptor {
                val request = it.request()
                val response = it.proceed(request)
                val requestUrl = request.url.toString()
                val domain = request.url.host
                // set-cookie maybe has multi, login to save cookie
                if ((requestUrl.contains(SAVE_USER_LOGIN_KEY) || requestUrl.contains(
                        SAVE_USER_REGISTER_KEY
                    ))
                    && response.headers(SET_COOKIE_KEY).isNotEmpty()
                ) {
                    val cookies = response.headers(SET_COOKIE_KEY)
                    val cookie = encodeCookie(cookies)
                    saveCookie(requestUrl, domain, cookie)
                }
                response
            }
            // set request cookie
            addInterceptor {
                val request = it.request()
                val builder = request.newBuilder()
                val domain = request.url.host
                // get domain cookie
                if (domain.isNotEmpty()) {
                    val spDomain: String = DataSaverUtils.readData(domain, "")
                    val cookie: String = if (spDomain.isNotEmpty()) spDomain else ""
                    if (cookie.isNotEmpty()) {
                        builder.addHeader(COOKIE_NAME, cookie)
                    }
                }
                it.proceed(builder.build())
            }
        }.build()
    }

    @JvmOverloads
    fun get(
        url: String,
        headersMap: HashMap<String, String>? = null
    ): String {
        val requestBuilder = Request.Builder().url(url).get()
        headersMap?.let {
            requestBuilder.addHeaders(it)
        }
        val response = okHttpClient.newCall(requestBuilder.build()).execute()
        return response.body?.string() ?: ""
    }

    @JvmOverloads
    fun getRaw(
        url: String,
        headersMap: HashMap<String, String>? = null
    ): ByteArray {
        val requestBuilder = Request.Builder().url(url).get()
        headersMap?.let {
            requestBuilder.addHeaders(it)
        }
        val response = okHttpClient.newCall(requestBuilder.build()).execute()
        return response.body?.bytes() ?: ByteArray(0)
    }

    @JvmOverloads
    fun getResponse(
        url: String,
        headersMap: HashMap<String, String>? = null
    ): Response {
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
        form: HashMap<String, String>,
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