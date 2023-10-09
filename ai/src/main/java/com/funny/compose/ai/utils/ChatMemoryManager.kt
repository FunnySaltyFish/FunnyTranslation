package com.funny.compose.ai.utils

import com.funny.compose.ai.bean.ChatMemory
import com.funny.compose.ai.bean.ChatMemoryFixedMsgLength

object ChatMemoryManager {
    fun getChatMemory(): ChatMemory {
        return ChatMemoryFixedMsgLength(2)
    }
}