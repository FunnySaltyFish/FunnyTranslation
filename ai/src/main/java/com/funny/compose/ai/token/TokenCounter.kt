package com.funny.compose.ai.token

import androidx.annotation.IntRange
import com.funny.compose.ai.bean.ChatMessageReq
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

    abstract suspend fun decode(tokens: List<Int>): String

    open suspend fun count(text: String, allowedSpecialTokens: Array<String> = arrayOf()) =
        encode(text, allowedSpecialTokens).size

    open suspend fun countMessages(messages: List<ChatMessageReq>) =
        messages.sumOf { msg ->
            count(msg.content)
        }

    open suspend fun truncate(
        text: String,
        allowedSpecialTokens: Array<String>,
        @IntRange(from = 0) maxTokenLength: Int = Int.MAX_VALUE
    ): String {
        val tokens = encode(text, allowedSpecialTokens, maxTokenLength)
        return decode(tokens)
    }
}

class OpenAITokenCounter(encodingName: String = "cl100k_base"): TokenCounter() {
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
    }

    override suspend fun decode(tokens: List<Int>): String {
        return enc.await().decode(tokens)
    }

    override suspend fun count(text: String, allowedSpecialTokens: Array<String>): Int {
        return encode(text, allowedSpecialTokens).size
    }

    // https://cookbook.openai.com/examples/how_to_count_tokens_with_tiktoken
    override suspend fun countMessages(messages: List<ChatMessageReq>): Int {
        val TOKENS_PER_MESSAGE = 3 // here we only use gpt-3.5-turbo and gpt-4, so the value is 3
        var numToken = 0
        for(msg in messages) {
            numToken += TOKENS_PER_MESSAGE
            numToken += count(msg.content)
            numToken += count(msg.role)
        }
        numToken += 3 // every reply is primed with <|start|>assistant<|message|>
        return numToken
    }

    override suspend fun truncate(
        text: String,
        allowedSpecialTokens: Array<String>,
        maxTokenLength: Int
    ): String {
        val result = enc.await().encodeOrdinary(text, maxTokenLength)
        return if (result.isTruncated) {
            decode(result.tokens)
        } else {
            text
        }
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

    override suspend fun decode(tokens: List<Int>): String {
        return ""
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