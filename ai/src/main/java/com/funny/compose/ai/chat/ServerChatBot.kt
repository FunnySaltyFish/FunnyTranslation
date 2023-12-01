package com.funny.compose.ai.chat

import android.util.Log
import com.funny.compose.ai.bean.ChatMemory
import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.bean.StreamMessage
import com.funny.translation.core.BuildConfig
import com.funny.translation.helper.JsonX
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.Serializable

@Serializable
class ChatMessageReq(
    val role: String,
    val content: String
) {
    companion object {
        fun text(content: String, role: String = "user") = ChatMessageReq(role, content)
        fun vision(content: Vision, role: String = "user") = ChatMessageReq(role, JsonX.toJson(content))
    }

    /*
    "content": [
        {"type": "text", "text": "What’s in this image?"},
        {
          "type": "image_url",
          "image_url": {
            "url": "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg",
          },
        },
      ],
     */
    class Vision(
        val content: List<Content>,
    ) {
        @Serializable
        class Content(
            val type: String,
            val text: String? = null,
            val image_url: ImageUrl? = null,
        ) {
            @Serializable
            class ImageUrl(
                val url: String,
            )
        }
    }
}

abstract class ServerChatBot(
    private val verbose: Boolean = BuildConfig.DEBUG,
) : ChatBot() {
    abstract val args: HashMap<String, Any?>

    abstract suspend fun sendRequest(
        prompt: String,
        messages: List<ChatMessageReq>,
        args: Map<String, Any?>
    ): Flow<String>

    override suspend fun chat(
        conversationId: String?,
        currentMessage: String,
        messages: List<ChatMessage>,
        systemPrompt: String,
        memory: ChatMemory,
    ): Flow<StreamMessage> {
        val includedMessages = memory.getIncludedMessages(messages)
//        val text = getFormattedText(systemPrompt, includedMessages)
//        log("formattedText: \n$text")
        val chatMessageReqList = includedMessages.map {
            ChatMessageReq.text(
                role = if (it.sendByMe) "user" else "assistant",
                content = it.content
            )
        }
        return sendRequest(systemPrompt, chatMessageReqList, args).map {
            when {
                it.startsWith("<<error>") -> {
                    val remaining = it.removePrefix("<<error>")
                    StreamMessage.Error(remaining)
                }
                it.startsWith("<<start>>") -> StreamMessage.Start
                it.startsWith("<<end>>") -> {
                    val remaining = it.removePrefix("<<end>>")
                    if (remaining != "") {
                        JsonX.fromJson<StreamMessage.End>(remaining)
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
                log("error: $err")
                emit(StreamMessage.Error(err.message ?: "Unknown error"))
            }
        }
    }

    fun log(msg: Any?) {
        if (verbose) {
            Log.d(name, msg.toString())
        }
    }
}