package com.funny.compose.ai

import com.funny.compose.ai.bean.ChatMemory
import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.bean.StreamMessage
import kotlinx.coroutines.flow.Flow

abstract class ChatBot {
    abstract val id: Int
    abstract val name: String
    abstract val avatar: String

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
