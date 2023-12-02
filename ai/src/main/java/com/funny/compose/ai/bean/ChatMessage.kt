package com.funny.compose.ai.bean

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

const val SENDER_ME = "Me"

@Keep
@Entity(tableName = "table_chat_history")
data class ChatMessage(
    @ColumnInfo
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo
    val botId: Int,
    @ColumnInfo
    val conversationId: String,
    @ColumnInfo
    val sender: String,
    @ColumnInfo
    var content: String,
    @ColumnInfo
    val type: Int = ChatMessageTypes.TEXT,
    // 是否在响应过程中出错
    val error: String? = null,
    @ColumnInfo
    val timestamp: Long = System.currentTimeMillis(),
) {
    val sendByMe: Boolean get() = sender == SENDER_ME
}

object ChatMessageTypes {
    const val TEXT = 0
    const val IMAGE = 1
    const val ERROR = 99
}