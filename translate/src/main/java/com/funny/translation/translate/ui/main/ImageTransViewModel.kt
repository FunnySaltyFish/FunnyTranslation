package com.funny.translation.translate.ui.main

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.translate.database.DefaultData
import com.funny.translation.translate.engine.ImageTranslationEngine
import com.funny.translation.translate.engine.ImageTranslationEngines
import com.funny.translation.translate.selectKey
import kotlinx.coroutines.Job

class ImageTransViewModel: ViewModel() {
    var imageUri: Uri? by mutableStateOf(null)
    var translateEngine: ImageTranslationEngine? by mutableStateOf(null)
    var translateJob: Job? = null

    init {
        translateEngine = DefaultData.bindImageEngines.firstOrNull {
            DataSaverUtils.readData(it.selectKey, false)
        } ?: ImageTranslationEngines.Baidu
    }

    fun updateImageUri(uri: Uri?){
        imageUri = uri
    }

    fun translate(){
        translateEngine ?: return

    }

    fun isTranslating() = translateJob?.isActive == true

}