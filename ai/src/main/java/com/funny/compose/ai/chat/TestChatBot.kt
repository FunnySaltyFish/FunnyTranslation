package com.funny.compose.ai.chat

import com.funny.compose.ai.bean.ChatMemory
import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.bean.StreamMessage
import com.funny.compose.ai.token.TokenCounter
import com.funny.compose.ai.token.TokenCounters
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TestChatBot: ChatBot() {
    override val id: Int = 0
    override val name: String = "Test"
    override val avatar: String = "https://c-ssl.duitang.com/uploads/blog/202206/12/20220612164733_72d8b.jpg"
    override val tokenCounter: TokenCounter = TokenCounters.defaultTokenCounter
    override val maxContextLength = 1000

    override suspend fun chat(
        conversationId: String?,
        currentMessage: String,
        messages: List<ChatMessage>,
        systemPrompt: String,
        memory: ChatMemory
    ): Flow<StreamMessage> =
        flow {
            emit(StreamMessage.Start)
            emit(StreamMessage.Part("Hello, I'm $name.\n"))
            delay(1000)
            emit(StreamMessage.Part("I'm a test chat bot.\n"))
            delay(1000)
            emit(StreamMessage.Part("I can't do anything."))
            delay(40)
            emit(StreamMessage.End())
        }
}