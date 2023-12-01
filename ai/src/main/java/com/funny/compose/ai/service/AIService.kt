package com.funny.compose.ai.service

import android.util.Log
import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.chat.ChatMessageReq
import com.funny.translation.helper.JSONObjectSerializer
import com.funny.translation.network.ServiceCreator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Streaming

private const val TAG = "AIService"

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
    suspend fun askStream(@Body req: AskStreamRequest): ResponseBody

    @GET("ai/get_models")
    suspend fun getChatModels() : List<Model>
}

val aiService by lazy {
    ServiceCreator.create(AIService::class.java)
}

//suspend fun Call<ResponseBody>.asFlow() = withContext(Dispatchers.IO) {
//    val response = this@asFlow.execute()
//    flow {
//        if (response.isSuccessful) {
//            response.body()?.byteStream()?.bufferedReader()?.use {
//                emit(it.readText().also { txt ->
//                    Log.d(TAG, "readText: $txt")
//                })
//            }
//        }
//    }
//}

suspend fun ResponseBody.asFlow() = withContext(Dispatchers.IO) {
    flow {
        val response = this@asFlow
        response.source().use { inputStream ->
//            emit(it.readText().also { txt ->
//                Log.d(TAG, "readText: $txt")
//            })
            val buffer = ByteArray(256)
            while (true) {
                val read = inputStream.read(buffer)
                if (read == -1) {
                    break
                }
                emit(String(buffer, 0, read).also {
                    Log.d(TAG, "readText: $it")
                })
            }
        }
    }
}
