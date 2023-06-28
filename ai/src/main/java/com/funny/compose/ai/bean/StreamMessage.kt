package com.funny.compose.ai.bean

sealed class StreamMessage(val id: String = "", val type: Int = ChatMessageTypes.TEXT) {
    object Start: StreamMessage()
    object End: StreamMessage()
    class Part(val part: String): StreamMessage()
}
