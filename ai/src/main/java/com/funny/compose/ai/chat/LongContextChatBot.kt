package com.funny.compose.ai.chat

import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.service.AIService
import com.funny.compose.ai.service.AskStreamRequest
import com.funny.compose.ai.token.TokenCounter
import com.funny.compose.ai.token.TokenCounters
import com.funny.translation.network.ServiceCreator
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

private val aiService by lazy {
    ServiceCreator.create(AIService::class.java)
}

class LongTextChatBot(
    val model: Model
) : ServerChatBot() {
    override val args: HashMap<String, Any?> by lazy { hashMapOf() }

    override suspend fun sendRequest(
        prompt: String,
        messages: List<ChatMessageReq>,
        args: Map<String, Any?>
    ): Flow<String> {
        return aiService.askStream(
            AskStreamRequest(
                modelId = model.chatBotId,
                messages = messages,
                prompt = prompt,
                args = JSONObject(args)
            )
        )
    }

    override val id: Int
        get() = model.chatBotId
    override val name: String
        get() = model.name

    override val avatar = "THIS DOES NOT USED"
    override val maxContextLength: Int
        get() = model.maxContextTokens

    override val tokenCounter: TokenCounter
        get() = TokenCounters.findById(model.tokenCounterId)
}