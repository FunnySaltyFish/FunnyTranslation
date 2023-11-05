package com.funny.compose.ai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TestServerChatBot: ServerChatBot() {
    override var args: HashMap<String, Any?> = hashMapOf()

    override suspend fun sendRequest(
        prompt: String,
        messages: List<ChatMessageReq>,
        args: Map<String, Any?>
    ): Flow<String> {
        return flow {
            emit("Hello, I'm $name.\n")
            emit("currentMessage: $prompt\n")
        }
    }

    override val id: Int = 0
    override val name: String = "Test"
    override val avatar: String = "https://c-ssl.duitang.com/uploads/blog/202206/12/20220612164733_72d8b.jpg"
    override val maxContextLength = 1024
}