package com.funny.compose.ai.service

import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.chat.ChatMessageReq
import com.funny.translation.helper.JSONObjectSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.json.JSONObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Streaming

@Serializable
data class AskStreamRequest(
    @SerialName("model_id") val modelId: Int,
    @SerialName("messages") val messages: List<ChatMessageReq>,
    @SerialName("prompt") val prompt: String,
    // args
    @Serializable(with = JSONObjectSerializer::class)
    @SerialName("args") val args: JSONObject,
)

interface AIService {
    @POST("ai/ask_stream")
    @Streaming
    suspend fun askStream(@Body req: AskStreamRequest): Flow<String>

    @GET("ai/get_models")
    suspend fun getChatModels() : List<Model>
}
