package com.funny.compose.ai.chat

import com.funny.compose.ai.bean.ChatMemory
import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.bean.ChatMessageReq
import com.funny.compose.ai.bean.StreamMessage
import com.funny.compose.ai.service.asStreamMessageFlow
import com.funny.compose.ai.token.TokenCounter
import com.funny.compose.ai.token.TokenCounters
import kotlinx.coroutines.flow.Flow

abstract class ChatBot {
    abstract val id: Int
    abstract val name: String
    abstract val avatar: String
    open val tokenCounter: TokenCounter = TokenCounters.defaultTokenCounter

    abstract val args: HashMap<String, Any?>
    /**
     * 单次接收文本的最大长度
     */
    abstract val maxContextTokens: Int

    abstract suspend fun sendRequest(
        prompt: String,
        messages: List<ChatMessageReq>,
        args: Map<String, Any?>
    ): Flow<String>

    open suspend fun chat(
        conversationId: String?,
        currentMessage: String,
        messages: List<ChatMessage>,
        systemPrompt: String,
        memory: ChatMemory,
        args: Map<String, Any?> = emptyMap(),
    ): Flow<StreamMessage> {
        val includedMessages = memory.getIncludedMessages(messages)
        val chatMessageReqList = includedMessages.map {
            ChatMessageReq.text(
                role = if (it.sendByMe) "user" else "assistant",
                content = it.content
            )
        }
        return sendRequest(systemPrompt, chatMessageReqList, args).asStreamMessageFlow()
    }
}

data class Conversation(
    val id: String,
    var messages: List<StreamMessage>? = null
)

object ChatBots {
    private val chatBots: Array<ModelChatBot> = arrayOf(
        TestLongTextChatBot(),
    )

    private val map = chatBots.associateBy { it.id }

    fun findById(id: Int) = map[id] ?: chatBots[0]
}