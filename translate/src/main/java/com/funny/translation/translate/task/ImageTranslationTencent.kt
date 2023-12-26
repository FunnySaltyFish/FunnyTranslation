package com.funny.translation.translate.task

import com.funny.translation.helper.Log
import com.funny.translation.translate.ImageTranslationTask
import com.funny.translation.translate.TranslationException
import com.funny.translation.translate.engine.ImageTranslationEngine
import com.funny.translation.translate.engine.ImageTranslationEngines
import com.funny.translation.translate.network.TransNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ImageTranslationTencent(): ImageTranslationTask(), ImageTranslationEngine by ImageTranslationEngines.Baidu {
     override suspend fun translate() {
         super.translate()
         withContext(Dispatchers.IO){
             val source = languageMapping[sourceLanguage]!!
             val target = languageMapping[targetLanguage]!!
             val file = sourceImg.toRequestBody()
             val body = MultipartBody.Builder()
                 .setType(MultipartBody.FORM)
                 .addFormDataPart("engine", "tencent")
                 .addFormDataPart("source", source)
                 .addFormDataPart("target", target)
                 .addFormDataPart("image", "image", file)
                 .build()
             val data = TransNetwork.imageTranslateService.getTransResult(body)
             if (data.code == 50) {
                 data.data?.let {
                     result = it
                     Log.d("ImgTransBaidu", "translate result: $it")
                 }
             } else {
                 throw TranslationException(data.displayErrorMsg)
             }
         }
     }

    override val isOffline: Boolean = false
}