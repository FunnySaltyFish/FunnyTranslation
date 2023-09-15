package com.funny.translation.translate

import com.funny.translation.core.R
import com.funny.translation.helper.string
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class ImageTranslationPart(
    val source: String,
    var target: String,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

@kotlinx.serialization.Serializable
data class ImageTranslationResult(
    @SerialName("erased_img")
    val erasedImgBase64: String? = null,
    val source: String = "",
    val target: String = "",
    val content: List<ImageTranslationPart> = arrayListOf()
)

abstract class ImageTranslationTask(
    var sourceImg: ByteArray = byteArrayOf(),
) : CoreTranslationTask() {
    var result = ImageTranslationResult()

    @Throws(TranslationException::class)
    open suspend fun translate(){
        if (!supportLanguages.contains(sourceLanguage) || !supportLanguages.contains(targetLanguage)){
            throw TranslationException(string(R.string.unsupported_language))
        }
        if (sourceLanguage == targetLanguage) return
    }

    abstract val isOffline: Boolean
}