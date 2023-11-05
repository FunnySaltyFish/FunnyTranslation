package com.funny.translation.translate.ui.long_text.bean

import androidx.annotation.Keep
import androidx.compose.runtime.Stable
import com.funny.translation.helper.BigDecimalSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * 可供选择的模型列表，包含模型名称、模型描述、模型价格（每千字符）
 * @constructor
 */
@Serializable
@Stable
@Keep
data class Model (
    @SerialName("chat_bot_id")
    val chatBotId: Int,
    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("cost_1k_chars")
    val cost1kChars: BigDecimal,
    val description: String,
)