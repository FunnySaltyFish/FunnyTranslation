package com.funny.compose.ai.chat

import com.funny.compose.ai.bean.ChatMemory
import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.bean.StreamMessage
import com.funny.compose.ai.token.TokenCounter
import com.funny.compose.ai.token.TokenCounters
import kotlinx.coroutines.flow.Flow

abstract class ChatBot {
    abstract val id: Int
    abstract val name: String
    abstract val avatar: String
    open val tokenCounter: TokenCounter = TokenCounters.defaultTokenCounter


    /**
     * 单次接收文本的最大长度
     */
    abstract val maxContextLength: Int
    abstract suspend fun chat(
        conversationId: String?,
        currentMessage: String,
        messages: List<ChatMessage>,
        systemPrompt: String,
        memory: ChatMemory
    ): Flow<StreamMessage>
}

data class Conversation(
    val id: String,
    var messages: List<StreamMessage>? = null
)

object ChatBots {
    private val chatBots: Array<ServerChatBot> = arrayOf(
        TestLongTextChatBot()
    )

    private val map = chatBots.associateBy { it.id }

    fun findById(id: Int) = map[id] ?: chatBots[0]
}