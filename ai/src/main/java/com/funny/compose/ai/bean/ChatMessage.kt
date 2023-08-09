package com.funny.compose.ai.bean

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

const val SENDER_ME = "Me"

@Keep
@Entity(tableName = "table_messages", primaryKeys = ["botId", "conversationId"])
data class ChatMessage(
    @ColumnInfo
    @PrimaryKey(autoGenerate = false)
    val id: String,
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
    @ColumnInfo
    val timestamp: Long = System.currentTimeMillis(),
) {
    val sendByMe: Boolean get() = sender == SENDER_ME
    // 是否正在响应中
    val responding: Boolean = true
    // 是否在响应过程中出错
    var error: String? = null

}

object ChatMessageTypes {
    const val TEXT = 0
    const val IMAGE = 1
    const val ERROR = 99
}