package com.funny.translation.translate.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.funny.compose.ai.bean.ChatMessage

@Dao
interface ChatHistoryDao {
    @Query("select * from table_chat_history where conversationId = :conversationId")
    fun getMessagesByConversationId(conversationId: String): List<ChatMessage>
    
    // clear all messages by conversationId
    @Query("delete from table_chat_history where conversationId = :conversationId")
    fun clearMessagesByConversationId(conversationId: String)

    @Insert
    fun insert(chatMessage: ChatMessage)

    @Delete
    fun delete(message: ChatMessage)
}