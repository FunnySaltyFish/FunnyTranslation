package com.funny.compose.ai.chat

import android.util.Log
import com.funny.compose.ai.bean.ChatMessageReq
import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.service.AskStreamRequest
import com.funny.compose.ai.service.aiService
import com.funny.compose.ai.service.asFlow
import com.funny.compose.ai.token.TokenCounter
import com.funny.compose.ai.token.TokenCounters
import com.funny.translation.ai.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.json.JSONObject

open class ModelChatBot(
    val model: Model
) : ChatBot() {
    private val verbose = BuildConfig.DEBUG
    override val args: HashMap<String, Any?> by lazy { hashMapOf() }

    override suspend fun sendRequest(
        prompt: String,
        messages: List<ChatMessageReq>,
        args: Map<String, Any?>
    ): Flow<String> {
        return try {
            aiService.askStream(
                AskStreamRequest(
                    modelId = model.chatBotId,
                    messages = messages,
                    prompt = prompt,
                    args = JSONObject(args)
                )
            ).asFlow()
        } catch (e: Exception) {
            e.printStackTrace()
            flowOf("<<error>>$e")
        }
    }

    fun log(msg: Any?) {
        if (verbose) {
            Log.d(name, msg.toString())
        }
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
