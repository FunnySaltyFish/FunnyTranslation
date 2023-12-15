package com.funny.compose.ai.service

import android.util.Log
import com.funny.compose.ai.bean.ChatMessageReq
import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.bean.StreamMessage
import com.funny.translation.AppConfig
import com.funny.translation.BaseApplication
import com.funny.translation.helper.JSONObjectSerializer
import com.funny.translation.helper.JsonX
import com.funny.translation.helper.toastOnUi
import com.funny.translation.network.CommonData
import com.funny.translation.network.ServiceCreator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming

private const val TAG = "AIService"
private val EmptyJsonObject = JSONObject()

@Serializable
data class AskStreamRequest(
    @SerialName("model_id") val modelId: Int,
    @SerialName("messages") val messages: List<ChatMessageReq>,
    @SerialName("prompt") val prompt: String,
    // args
    @Serializable(with = JSONObjectSerializer::class)
    @SerialName("args") val args: JSONObject = EmptyJsonObject,
)

@Serializable
data class CountTokenMessagesRequest(
    @SerialName("token_counter_id") val tokenCounterId: String,
    @SerialName("messages") val messages: List<ChatMessageReq>,
)

interface AIService {
    /**
     * 会返回流，包括开始的 <<start>> 、<<error>> 和结束的 <<end>>
     * @param req AskStreamRequest
     * @return ResponseBody
     */
    @POST("ai/ask_stream")
    // 设置超时时间
    @Headers("Cache-Control: no-cache", "CONNECT_TIMEOUT: 15", "READ_TIMEOUT: 3000", "WRITE_TIMEOUT: 10")
    @Streaming
    suspend fun askStream(@Body req: AskStreamRequest): ResponseBody

    @GET("ai/get_models")
    suspend fun getChatModels() : List<Model>

    @POST("ai/count_tokens_text")
    @FormUrlEncoded
    suspend fun countTokensText(
        @Field("token_counter_id") tokenCounterId: String,
        @Field("text") text: String,
        @Field("max_length") maxLength: Int? = null,
    ): CommonData<Int>

    @POST("ai/count_tokens_messages")
    suspend fun countTokensMessages(
        @Body req: CountTokenMessagesRequest
    ): CommonData<Int>

    @GET("ai/truncate_text")
    suspend fun truncateText(
        @Query("token_counter_id") tokenCounterId: String,
        @Query("text") text: String,
        @Query("max_length") maxLength: Int,
    ): CommonData<String>

}

val aiService by lazy {
    ServiceCreator.create(AIService::class.java)
}

suspend fun AIService.askAndParseStream(req: AskStreamRequest): Flow<StreamMessage> {
    return askStream(req).asFlow().asStreamMessageFlow()
}

/**
问一个问题并直接返回结果，默认处理掉 `<<start>>` 和 `<<end>>`，返回中间的部分，并且自动完成扣费
 */
suspend fun AIService.askAndProcess(
    req: AskStreamRequest,
    onError: (String) -> Unit = { BaseApplication.ctx.toastOnUi(it) },
): String {
    val output = StringBuilder()
    askAndParseStream(req).collect {
        when (it) {
            is StreamMessage.Error -> {
                onError(it.error)
            }
            is StreamMessage.Part -> {
                output.append(it.part)
            }
            else -> Unit
        }
    }
    return output.toString()
}

suspend fun ResponseBody.asFlow() = withContext(Dispatchers.IO) {
    flow {
        val response = this@asFlow
        response.source().use { inputStream ->
            val buffer = ByteArray(256)
            try {
                while (true) {
                    val read = inputStream.read(buffer)
                    if (read == -1) {
                        break
                    }
                    emit(String(buffer, 0, read).also { Log.d(TAG, "asFlow: $it") })
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emit("<<error>>" + e.message)
            }
        }
    }
}

/**
 * 将一个字符串流转换为 StreamMessage 的流，并自动完成扣费
 * @receiver Flow<String>
 * @return Flow<StreamMessage>
 */
suspend fun Flow<String>.asStreamMessageFlow() = map {
    when {
        it.startsWith("<<error>>") -> {
            val remaining = it.removePrefix("<<error>>")
            StreamMessage.Error(remaining)
        }
        it.startsWith("<<start>>") -> StreamMessage.Start
        it.startsWith("<<end>>") -> {
            val remaining = it.removePrefix("<<end>>")
            if (remaining != "") {
                JsonX.fromJson<StreamMessage.End>(remaining).also {
                    AppConfig.subAITextPoint(it.consumption)
                }
            } else {
                StreamMessage.End()
            }
        }
        else -> StreamMessage.Part(it)
    }
}.onStart {
    emit(StreamMessage.Start)
}.onCompletion { err ->
    if (err != null) {
        emit(StreamMessage.Error(err.message ?: "Unknown error"))
    }
}