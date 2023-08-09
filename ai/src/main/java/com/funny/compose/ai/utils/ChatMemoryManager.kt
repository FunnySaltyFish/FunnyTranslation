package com.funny.compose.ai.utils

import com.funny.compose.ai.bean.ChatMemory
import com.funny.compose.ai.bean.ChatMemoryFixedLength

object ChatMemoryManager {
    fun getChatMemory(): ChatMemory {
        return ChatMemoryFixedLength(2)
    }
}