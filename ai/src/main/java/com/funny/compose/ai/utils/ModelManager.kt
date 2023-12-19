package com.funny.compose.ai.utils

import com.funny.compose.ai.service.aiService
import com.funny.translation.helper.lazyPromise

object ModelManager {
    val models by lazyPromise {
        kotlin.runCatching {
            aiService.getChatModels()
        }.onFailure { it.printStackTrace() }.getOrDefault(listOf())
    }
}