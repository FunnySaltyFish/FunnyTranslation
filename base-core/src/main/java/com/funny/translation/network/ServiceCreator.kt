package com.funny.translation.network

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

/** from https://github.com/zhujiang521/PlayAndroid
 * 版权：Zhujiang 个人版权
 * @author zhujiang
 * 版本：1.5
 * 创建日期：2021/4/30
 * 描述：ServiceCreator
 *
 * 有修改
 */

object ServiceCreator {

    const val BASE_URL = "https://api.funnysaltyfish.fun/trans/v1/"

    /**
     * 自定义，适配特殊数据类型
     */
    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd hh:mm:ss")
        .registerTypeAdapter(Date::class.java, object : JsonDeserializer<Date> {
            override fun deserialize(
                json: JsonElement?,
                typeOfT: Type?,
                context: JsonDeserializationContext?
            ): Date {
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
                return simpleDateFormat.parse(json?.asString?:"2021-01-01 00:00:00")!!
            }
        })  // yyyy-MM-dd HH:mm:ss
        .create()

    private val retrofit by lazy{
        val appName = "FunnyTranslation"
        val okHttpClient = OkHttpUtils.okHttpClient.newBuilder().addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .addHeader("User-Agent", appName)
                    .addHeader("Referee", appName)
                    .build()
            )
        }.build()
        RetrofitBuild(
            url = BASE_URL,
            client = okHttpClient,
            gsonFactory = GsonConverterFactory.create(gson)
        ).retrofit
    }

    /**
     * get ServiceApi
     */
    fun <T> create(service: Class<T>): T = retrofit.create(service)
}


class RetrofitBuild(
    url: String, client: OkHttpClient,
    gsonFactory: GsonConverterFactory
) {
    val retrofit: Retrofit = Retrofit.Builder().apply {
        baseUrl(url)
        client(client)
        addConverterFactory(gsonFactory)
    }.build()
}

/**
 * save cookie string
 */
fun encodeCookie(cookies: List<String>): String {
    val sb = StringBuilder()
    val set = HashSet<String>()
    cookies
        .map { cookie ->
            cookie.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        }
        .forEach { it ->
            it.filterNot { set.contains(it) }.forEach { set.add(it) }
        }

    val ite = set.iterator()
    while (ite.hasNext()) {
        val cookie = ite.next()
        sb.append(cookie).append(";")
    }

    val last = sb.lastIndexOf(";")
    if (sb.length - 1 == last) {
        sb.deleteCharAt(last)
    }

    return sb.toString()
}
