package com.funny.translation.network

import android.content.Intent
import android.util.Log
import androidx.annotation.Keep
import com.funny.translation.AppConfig
import com.funny.translation.BaseApplication
import com.funny.translation.Consts
import com.funny.translation.TranslateConfig
import com.funny.translation.bean.UserBean
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.toastOnUi
import com.funny.translation.sign.SignUtils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

@Keep
object OkHttpUtils {
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

    private fun createBaseClient() = OkHttpClient().newBuilder().apply {
        connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        cache(cache)
        addInterceptor(HttpCacheInterceptor())
        // set request cookie
        // 添加自定义请求头
        addInterceptor {
            val request = it.request()
            val builder = request.newBuilder()
            val newUrl = URL(removeExtraSlashOfUrl(request.url.toString())).also { url -> builder.url(url) }
            val domain = request.url.host
            // get domain cookie
            if (domain.isNotEmpty()) {
                val spDomain: String = DataSaverUtils.readData(domain, "")
                val cookie: String = spDomain.ifEmpty { "" }
                if (cookie.isNotEmpty()) {
                    builder.addHeader(COOKIE_NAME, cookie)
                }
            }
            // 对所有向本项目请求的域名均加上应用名称
            if (newUrl.host.contains(NetworkConfig.API_HOST)){
                builder.addHeader("Referer", "FunnyTranslation")
                builder.addHeader("User-Agent", "FunnyTranslation/${AppConfig.versionCode}")
            }

            // 访问 trans/v1下的所有api均带上请求头-jwt
            if (newUrl.path.startsWith(NetworkConfig.TRANS_PATH)){
                val jwt = AppConfig.jwtToken
                if (jwt != "") builder.addHeader("Authorization", "Bearer $jwt")
            }

            if (newUrl.path.startsWith(NetworkConfig.TRANS_PATH + "api/translate")){
                builder.addHeader("sign", SignUtils.encodeSign(
                    uid = AppConfig.uid.toLong(), appVersionCode = AppConfig.versionCode,
                    sourceLanguageCode = TranslateConfig.sourceLanguage.id,
                    targetLanguageCode = TranslateConfig.targetLanguage.id,
                    text = TranslateConfig.sourceString,
                    extra = ""
                ).also {
                    Log.d(TAG, "createBaseClient: add sign: $it")
                })
            }

            it.proceed(builder.build())
        }

        // get response cookie
        addInterceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            val requestUrl = request.url.toString()
            val domain = request.url.host

            // token 过期了
            if (response.code == 401 && requestUrl.startsWith(NetworkConfig.BASE_URL)){
                val clazz = Class.forName("com.funny.trans.login.LoginActivity")
                val intent = Intent().apply {
                    setClass(BaseApplication.ctx, clazz)
                }
                val activity = BaseApplication.getCurrentActivity()
                activity?.let {
                    AppConfig.userInfo.value = UserBean()
                    it.startActivity(intent)
                    it.toastOnUi("您的登录状态已过期，请重新登陆")
                }

                return@addInterceptor response
            }

            // set-cookie maybe has multi, login to save cookie
            if (response.headers(SET_COOKIE_KEY).isNotEmpty()) {
                val cookies = response.headers(SET_COOKIE_KEY)
                val cookie = encodeCookie(cookies)
                saveCookie(requestUrl, domain, cookie)
            }
            response
        }

    }.build()

    val okHttpClient by lazy {
        createBaseClient()
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

    fun postMultiForm(
        url: String,
        body: RequestBody,
        headers: HashMap<String, String>? = null,
    ): String {
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

    fun removeExtraSlashOfUrl(url: String): String {
        return if (url.isEmpty()) {
            url
        } else url.replace("(?<!(http:|https:))/+".toRegex(), "/")
    }
}