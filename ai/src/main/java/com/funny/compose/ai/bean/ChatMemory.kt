package com.funny.compose.ai.bean

import java.util.Date
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

abstract class ChatMemory {
    /**
     * 获取实际要发送的消息，注意不包含最初的 Prompt
     * @param list List<ChatMessage>
     * @return List<ChatMessage>
     */
    abstract fun getIncludedMessages(list: List<ChatMessage>) : List<ChatMessage>

    companion object {
        val Saver = { chatMemory: ChatMemory ->
            when(chatMemory) {
                is ChatMemoryFixedMsgLength -> "chat_memory#fixed_length#${chatMemory.length}"
                is ChatMemoryFixedDuration -> "chat_memory#fixed_duration#${chatMemory.duration.inWholeMilliseconds}"
                else -> ""
            }
        }

        val Restorer = lambda@ { str: String ->
            val parts = str.split("#")
            if (parts.size != 3 || parts[0] != "chat_memory") {
                return@lambda DEFAULT_CHAT_MEMORY
            }
            when(parts[1]) {
                "fixed_length" -> ChatMemoryFixedMsgLength(parts[2].toInt())
                "fixed_duration" -> ChatMemoryFixedDuration(parts[2].toLong().milliseconds)
                else -> DEFAULT_CHAT_MEMORY
            }
        }
    }
}

class ChatMemoryFixedMsgLength(val length: Int) : ChatMemory() {
    override fun getIncludedMessages(list: List<ChatMessage>): List<ChatMessage> {
        return list.takeLast(length)
    }
}

class ChatMemoryFixedDuration(val duration: Duration) : ChatMemory() {
    override fun getIncludedMessages(list: List<ChatMessage>): List<ChatMessage> {
        val now = Date()
        val last = list.lastOrNull { it.timestamp > now.time - duration.inWholeMilliseconds }
        return if (last == null) {
            emptyList()
        } else {
            list.dropWhile { it.timestamp < last.timestamp }
        }
    }
}

class ChatMemoryMaxContextSize(var maxContextSize: Int, var systemPrompt: String): ChatMemory() {
    override fun getIncludedMessages(list: List<ChatMessage>): List<ChatMessage> {
        var idx = list.size - 1
        var curLength = systemPrompt.length
        while (curLength < maxContextSize && idx >= 0) {
            curLength += list[idx].content.length
            idx--
        }
        return list.subList(idx + 1, list.size)
    }

}

private val DEFAULT_CHAT_MEMORY = ChatMemoryFixedMsgLength(2)