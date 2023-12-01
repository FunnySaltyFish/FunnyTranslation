package com.funny.compose.ai.chat

import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.service.AskStreamRequest
import com.funny.compose.ai.service.aiService
import com.funny.compose.ai.service.asFlow
import com.funny.compose.ai.token.TokenCounter
import com.funny.compose.ai.token.TokenCounters
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

class ModelChatBot(
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
        ).asFlow()
    }

    override val id: Int by model::chatBotId
    override val name: String by model::name
    override val avatar by model::avatar
    override val maxContextLength: Int by model::maxContextTokens

    override val tokenCounter: TokenCounter
        get() = TokenCounters.findById(model.tokenCounterId)

    companion object {
        val Empty = ModelChatBot(Model.Empty)
    }
}
