package com.funny.translation.translate.ui.main

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.Consts
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.translate.Language
import com.funny.translation.translate.database.DefaultData
import com.funny.translation.translate.engine.ImageTranslationEngine
import com.funny.translation.translate.engine.ImageTranslationEngines
import com.funny.translation.translate.findLanguageById
import com.funny.translation.translate.selectKey
import kotlinx.coroutines.Job

class ImageTransViewModel: ViewModel() {
    var imageUri: Uri? by mutableStateOf(null)
    var translateEngine: ImageTranslationEngine? by mutableStateOf(null)
    var translateJob: Job? = null
    var sourceLanguage by mutableDataSaverStateOf(
        DataSaverUtils,
        "key_img_source_lang",
        Language.ENGLISH
    )

    var targetLanguage by mutableDataSaverStateOf(
        DataSaverUtils,
        "key_img_target_lang",
        Language.CHINESE
    )

    init {
        translateEngine = DefaultData.bindImageEngines.firstOrNull {
            DataSaverUtils.readData(it.selectKey, false)
        } ?: ImageTranslationEngines.Baidu
    }



    fun translate(){
        translateEngine ?: return
    }

    fun isTranslating() = translateJob?.isActive == true
    fun updateImageUri(uri: Uri?){ imageUri = uri }
    fun updateSourceLanguage(language: Language){ sourceLanguage = language }
    fun updateTargetLanguage(language: Language){ targetLanguage = language }
}