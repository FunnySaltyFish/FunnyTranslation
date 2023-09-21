package com.funny.translation.translate.utils

import android.net.Uri
import com.funny.translation.AppConfig
import com.funny.translation.translate.Language

object DeepLinkManager {
    const val PREFIX = "funny://translation"
    const val TEXT_TRANS_PATH = "/translate"
    const val IMAGE_TRANS_PATH = "/translate_image"

    fun buildTextTransUri(sourceText: String?, sourceLanguage: Language?, targetLanguage: Language?, byFloatWindow: Boolean = false): Uri = Uri.parse(
        PREFIX + TEXT_TRANS_PATH).buildUpon()
            .appendQueryParameter("text", sourceText)
            .appendQueryParameter("sourceId", (sourceLanguage ?: AppConfig.sDefaultSourceLanguage.value).id.toString())
            .appendQueryParameter("targetId", (targetLanguage ?: AppConfig.sDefaultTargetLanguage.value).id.toString())
            .appendQueryParameter("byFloatWindow", byFloatWindow.toString())
            .build()

    fun buildImageTransUri(imageUri: Uri?, sourceLanguage: Language? = null, targetLanguage: Language? = null): Uri =
        Uri.parse(PREFIX + IMAGE_TRANS_PATH).buildUpon()
            .appendQueryParameter("imageUri", imageUri.toString())
            .appendQueryParameter("sourceId", (sourceLanguage ?: AppConfig.sDefaultSourceLanguage.value).id.toString())
            .appendQueryParameter("targetId", (targetLanguage ?: AppConfig.sDefaultTargetLanguage.value).id.toString())
            .build()

}