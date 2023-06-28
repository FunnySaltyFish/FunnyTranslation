package com.funny.compose.ai

import com.funny.compose.ai.bean.StreamMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TestChatBot: ChatBot() {
    override val id: Int = 0
    override val name: String = "Test"
    override val avatar: String = "https://c-ssl.duitang.com/uploads/blog/202206/12/20220612164733_72d8b.jpg"
    override var convIds: List<String>
        get() = listOf("convIdTest")
        set(value) {}

    override suspend fun chat(conversationId: String?, message: String): Flow<StreamMessage> =
        flow {
            emit(StreamMessage.Start)
            emit(StreamMessage.Part("Hello, I'm $name.\n"))
            delay(1000)
            emit(StreamMessage.Part("I'm a test chat bot.\n"))
            delay(1000)
            emit(StreamMessage.Part("I can't do anything."))
            delay(40)
            emit(StreamMessage.End)
        }
}