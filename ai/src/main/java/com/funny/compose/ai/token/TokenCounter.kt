package com.funny.compose.ai.token

import androidx.annotation.IntRange
import com.funny.translation.helper.lazyPromise
import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.EncodingType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.jvm.optionals.getOrNull


abstract class TokenCounter {
    abstract suspend fun encode(
        text: String,
        allowedSpecialTokens: Array<String>,
        @IntRange(from = 0) maxTokenLength: Int = Int.MAX_VALUE
    ): List<Int>

    open suspend fun count(text: String, allowedSpecialTokens: Array<String> = arrayOf()) =
        encode(text, allowedSpecialTokens).size

    open suspend fun countMessages(systemPrompt: String, messages: List<String>) =
        count(systemPrompt) + messages.sumOf { count(it) }
}

class OpenAITokenCounter(encodingName: String): TokenCounter() {
    // getEncoding 是一个非常非常非常耗时的操作，所以用协程懒加载
    private val enc by lazyPromise(CoroutineScope(Dispatchers.IO)) {
        val registry = Encodings.newDefaultEncodingRegistry()
        val type = EncodingType.fromName(encodingName).getOrNull() ?: EncodingType.CL100K_BASE
        registry.getEncoding(type)
    }

    /**
     * Encode text 至 token 数组，allowedSpecialTokens 暂不支持
     * @param text String
     * @param allowedSpecialTokens Array<String>
     * @param maxTokenLength Int
     * @return List<Int>
     */
    override suspend fun encode(
        text: String,
        allowedSpecialTokens: Array<String>,
        maxTokenLength: Int
    ): List<Int> {
        return enc.await().encodeOrdinary(text, maxTokenLength).tokens
//        return encoding.await().encode(text, allowedSpecialTokens, maxTokenLength.toLong()).map { it.toInt() }
    }

    override suspend fun count(text: String, allowedSpecialTokens: Array<String>): Int {
        return encode(text, allowedSpecialTokens).size
    }

    override suspend fun countMessages(systemPrompt: String, messages: List<String>): Int {
        return super.countMessages(systemPrompt, messages)
    }
}

/**
 * 默认的 TokenCounter，直接用文本长度作为 token 数量
 */
class DefaultTokenCounter: TokenCounter() {
    override suspend fun encode(
        text: String,
        allowedSpecialTokens: Array<String>,
        maxTokenLength: Int
    ): List<Int> {
        return emptyList()
    }

    override suspend fun count(text: String, allowedSpecialTokens: Array<String>): Int {
        return text.length
    }
}

object TokenCounters {
    private val tokenCounters = hashMapOf<Int, TokenCounter>(
         1 to OpenAITokenCounter("cl100k_base"),
//        1 to OpenAITokenCounter(encodingName = "gpt-3.5-turbo"),
        2 to OpenAITokenCounter("gpt4"),
    )

    val defaultTokenCounter = DefaultTokenCounter()

    fun findById(id: Int) = tokenCounters[id] ?: defaultTokenCounter
}