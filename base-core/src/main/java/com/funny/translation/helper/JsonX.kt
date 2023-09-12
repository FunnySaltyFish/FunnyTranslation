package com.funny.translation.helper

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import java.lang.reflect.Type
import kotlin.reflect.KClass

@OptIn(ExperimentalSerializationApi::class)
object JsonX {
    val formatter by lazy(LazyThreadSafetyMode.PUBLICATION) {
        Json {
            ignoreUnknownKeys = true // JSON和数据模型字段可以不匹配
            coerceInputValues = true // 如果JSON字段是Null则使用默认值
            allowStructuredMapKeys = true
        }
    }

    val formatterPretty by lazy(LazyThreadSafetyMode.PUBLICATION) {
        Json {
            ignoreUnknownKeys = true // JSON和数据模型字段可以不匹配
            coerceInputValues = true // 如果JSON字段是Null则使用默认值
            prettyPrint = true
            prettyPrintIndent = "  "
        }
    }

    inline fun <reified T : Any> fromJson(json: String): T = formatter.decodeFromString(json)
    inline fun <reified T : Any> fromJson(json: String, clazz: KClass<T>): T = formatter.decodeFromString(json)
    inline fun <reified T: Any> fromJson(json: String, clazz: Class<T>) = formatter.decodeFromString<T>(json)
    inline fun <reified T: Any> fromJson(json: String, type: Type) = formatter.decodeFromString<T>(json)

    inline fun <reified T: Any> toJson(bean: T) = formatter.encodeToString(bean)
    inline fun <reified T: Any> toJsonPretty(bean: T) = formatterPretty.encodeToString(bean)

    fun asConverterFactory(mediaType: MediaType) = formatter.asConverterFactory(mediaType)
}