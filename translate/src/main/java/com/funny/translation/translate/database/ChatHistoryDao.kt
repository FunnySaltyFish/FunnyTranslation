package com.funny.translation.translate.database

import androidx.room.Dao
import androidx.room.Query
import com.funny.compose.ai.bean.ChatMessage

@Dao
interface ChatHistoryDao {
    @Query("select * from table_messages where conversationId = :conversationId")
    fun getMessagesByConversationId(conversationId: String): List<com.funny.compose.ai.bean.ChatMessage>
}