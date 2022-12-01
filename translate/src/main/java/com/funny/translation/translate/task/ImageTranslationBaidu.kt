package com.funny.translation.translate.task

import com.funny.translation.translate.ImageTranslationTask
import com.funny.translation.translate.engine.ImageTranslationEngine
import com.funny.translation.translate.engine.ImageTranslationEngines
import com.funny.translation.translate.network.TransNetwork
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class ImageTranslationBaidu(): ImageTranslationTask(), ImageTranslationEngine by ImageTranslationEngines.Baidu {
    override fun translate() {
        val source = languageMapping[sourceLanguage]!!
        val target = languageMapping[targetLanguage]!!
        val file = sourceImg.toRequestBody()
        val data = TransNetwork.imageTranslateService.getTransResult("baidu", source, target, file)
    }

    override val isOffline: Boolean
        get() = TODO("Not yet implemented")

}