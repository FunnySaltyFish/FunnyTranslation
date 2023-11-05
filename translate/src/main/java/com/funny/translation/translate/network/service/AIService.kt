package com.funny.translation.translate.network.service

import com.funny.compose.ai.ChatMessageReq
import com.funny.translation.translate.ui.long_text.bean.Model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

@Serializable
data class AskStreamRequest(
    @SerialName("model_id") val modelId: Int,
    @SerialName("chat_message_req") val chatMessageReq: ChatMessageReq
)

interface AIService {
    @POST("ai/ask_stream")
    fun askStream(@Body req: AskStreamRequest)

    @GET("ai/get_models")
    suspend fun getChatModels() : List<Model>

}
