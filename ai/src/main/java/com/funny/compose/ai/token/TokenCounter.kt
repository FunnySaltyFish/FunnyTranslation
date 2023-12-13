package com.funny.compose.ai.token

import androidx.annotation.IntRange
import com.funny.compose.ai.bean.ChatMessageReq
import com.funny.compose.ai.service.CountTokenMessagesRequest
import com.funny.compose.ai.service.aiService
import com.funny.translation.helper.lazyPromise
import com.funny.translation.network.api
import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.EncodingType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.jvm.optionals.getOrNull


abstract class TokenCounter {
    /**
     * count 的操作是否是在服务端完成的
     */
    abstract val online: Boolean
    abstract val id: String

    abstract suspend fun count(text: String, allowedSpecialTokens: Array<String> = arrayOf()): Int

    abstract suspend fun truncate(
        text: String,
        allowedSpecialTokens: Array<String>,
        @IntRange(from = 0) maxTokenLength: Int = Int.MAX_VALUE
    ): String

    open suspend fun countMessages(messages: List<ChatMessageReq>) =
        messages.sumOf {
            count(it.content) + count(it.role)
        }
}

class OpenAITokenCounter(encodingName: String = "cl100k_base"): TokenCounter() {
    override val online: Boolean = false
    override val id: String = "openai"

    // getEncoding 是一个非常非常非常耗时的操作，所以用协程懒加载
    private val enc by lazyPromise(CoroutineScope(Dispatchers.IO)) {
        val registry = Encodings.newDefaultEncodingRegistry()
        val type = EncodingType.fromName(encodingName).getOrNull() ?: EncodingType.CL100K_BASE
        registry.getEncoding(type)
    }

    override suspend fun count(text: String, allowedSpecialTokens: Array<String>): Int {
        return enc.await().encodeOrdinary(text).size
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
            enc.await().decode(result.tokens)
        } else {
            text
        }
    }
}

class ServerTokenCounter(override val id: String): TokenCounter() {
    override val online: Boolean = true
    override suspend fun count(text: String, allowedSpecialTokens: Array<String>): Int {
        return api(aiService::countTokensText, id, text, null) {
            success {  }
        } ?: 0
    }

    override suspend fun countMessages(messages: List<ChatMessageReq>): Int {
        return api(aiService::countTokensMessages, CountTokenMessagesRequest(id, messages)) {
            success {  }
        } ?: 0
    }

    override suspend fun truncate(
        text: String,
        allowedSpecialTokens: Array<String>,
        maxTokenLength: Int
    ): String {
        return api(aiService::truncateText, id, text, maxTokenLength) {
            success {  }
        } ?: text
    }
}

/**
 * 默认的 TokenCounter，直接用文本长度作为 token 数量
 */
class DefaultTokenCounter: TokenCounter() {
    override val online: Boolean = false
    override val id: String = "default"

    override suspend fun count(text: String, allowedSpecialTokens: Array<String>): Int {
        return text.length
    }

    override suspend fun truncate(
        text: String,
        allowedSpecialTokens: Array<String>,
        maxTokenLength: Int
    ): String {
        return text.substring(0, maxTokenLength.coerceAtMost(text.length))
    }
}

object TokenCounters {
    val defaultTokenCounter = DefaultTokenCounter()
    private val tokenCounters = hashMapOf<String, TokenCounter>(
        "openai" to OpenAITokenCounter("cl100k_base"),
        "default" to defaultTokenCounter,
    )

    fun findById(id: String) = tokenCounters[id] ?: ServerTokenCounter(id).also {
        tokenCounters[id] = it
    }
}