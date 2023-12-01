package com.funny.compose.ai.bean

import com.funny.translation.helper.BigDecimalSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
sealed class StreamMessage(val id: String = "", val type: Int = ChatMessageTypes.TEXT) {
    object Start: StreamMessage()
    @Serializable
    class End(
        val input_tokens: Int = 0,
        val output_tokens: Int = 0,
        @Serializable(with = BigDecimalSerializer::class)
        val consumption: BigDecimal = BigDecimal.ZERO
    ): StreamMessage()
    class Part(val part: String): StreamMessage()
    class Error(val error: String): StreamMessage()
}
