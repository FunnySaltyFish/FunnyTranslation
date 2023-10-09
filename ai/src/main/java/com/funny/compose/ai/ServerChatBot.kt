package com.funny.compose.ai

import android.util.Log
import com.funny.compose.ai.bean.ChatMemory
import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.bean.ChatMessageTypes
import com.funny.compose.ai.bean.StreamMessage
import com.funny.translation.core.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart


abstract class ServerChatBot(
    private val verbose: Boolean = BuildConfig.DEBUG,
) : ChatBot() {
    abstract var args: HashMap<String, Any?>

    abstract suspend fun sendRequest(
        formattedText: String,
        args: Map<String, Any?>
    ): Flow<String>

    open fun getFormattedText(systemPrompt: String, includedMessages: List<ChatMessage>)  = buildString {
        append(systemPrompt)
        append("\n")
        includedMessages.forEach {
            append(it.formatAsSendText())
            append("\n")
        }
        append("AI:")
    }

    override suspend fun chat(
        conversationId: String?,
        currentMessage: String,
        messages: List<ChatMessage>,
        systemPrompt: String,
        memory: ChatMemory,
    ): Flow<StreamMessage> {
        val includedMessages = memory.getIncludedMessages(messages)
        val text = getFormattedText(systemPrompt, includedMessages)
        log("formattedText: \n$text")
        return sendRequest(text, args).map {
            when {
                it.startsWith("<<error>") -> StreamMessage.Error(it.removePrefix("<<error>"))
                it.startsWith("<<start>>") -> StreamMessage.Start
                else -> StreamMessage.Part(it)
            }
        }.onStart {
            emit(StreamMessage.Start)
        }.onCompletion { err ->
            if (err != null) {
                log("error: $err")
                emit(StreamMessage.Error(err.message ?: "Unknown error"))
            }
            emit(StreamMessage.End)
        }
    }

    fun log(msg: Any?) {
        if (verbose) {
            Log.d(name, msg.toString())
        }
    }
}

fun ChatMessage.formatAsSendText(): String {
    val sender = if(sendByMe) "User" else "AI"
    val msg = when(type) {
        ChatMessageTypes.TEXT -> content
        else -> content
    }
    return "$sender: $msg"
}