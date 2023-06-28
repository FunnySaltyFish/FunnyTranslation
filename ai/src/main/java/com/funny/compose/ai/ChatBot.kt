package com.funny.compose.ai

import com.funny.compose.ai.bean.StreamMessage
import kotlinx.coroutines.flow.Flow

abstract class ChatBot {
    abstract val id: Int
    abstract val name: String
    abstract val avatar: String
    abstract suspend fun chat(conversationId: String?, message: String): Flow<StreamMessage>
    abstract var convIds : List<String>
}

data class Conversation(
    val id: String,
    var messages: List<StreamMessage>? = null
)