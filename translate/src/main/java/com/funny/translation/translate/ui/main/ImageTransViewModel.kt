package com.funny.translation.translate.ui.main

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.compose.loading.LoadingState
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.AppConfig
import com.funny.translation.TranslateConfig
import com.funny.translation.helper.BitmapUtil
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.ImageTranslationResult
import com.funny.translation.translate.Language
import com.funny.translation.translate.appCtx
import com.funny.translation.translate.database.DefaultData
import com.funny.translation.translate.engine.ImageTranslationEngine
import com.funny.translation.translate.engine.ImageTranslationEngines
import com.funny.translation.translate.engine.selectKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ImageTransViewModel: ViewModel() {
    var imageUri: Uri? by mutableStateOf(null)
    var translateEngine: ImageTranslationEngine by mutableStateOf(ImageTranslationEngines.Baidu)
    private var translateJob: Job? = null
    var translateState: LoadingState<ImageTranslationResult> by mutableStateOf(LoadingState.Loading)

    var sourceLanguage by mutableDataSaverStateOf(DataSaverUtils, "key_img_source_lang", Language.ENGLISH)
    var targetLanguage by mutableDataSaverStateOf(DataSaverUtils, "key_img_target_lang", Language.CHINESE)

    var imgWidth = 0
    var imgHeight = 0

    var allEngines = arrayListOf(ImageTranslationEngines.Baidu, ImageTranslationEngines.Tencent)

    init {
        translateEngine = DefaultData.bindImageEngines.firstOrNull {
            DataSaverUtils.readData(it.selectKey, false)
        } ?: ImageTranslationEngines.Baidu
    }

    fun translate(){
        imageUri ?: return
        translateJob?.cancel()
        Log.d(TAG, "translate: start")
        translateJob = viewModelScope.launch(Dispatchers.IO) {
            translateState = LoadingState.Loading
            // 延迟一下再开始翻译，防止语言选的不对临时变更
            delay(1000)
            kotlin.runCatching {
                val bytes = BitmapUtil.getBitmapFormUri(appCtx, 4096, 4096, 2 * 1024 * 1024, imageUri!!)
                Log.d(TAG, "translate: imageSize: ${bytes.size}")
                with(TranslateConfig){
                    this.sourceLanguage = this@ImageTransViewModel.sourceLanguage
                    this.targetLanguage = this@ImageTransViewModel.targetLanguage
                    this.sourceString   = ""
                }
                translateEngine.createTask(bytes, sourceLanguage, targetLanguage).apply {
                    this.translate()
                }.result
            }.onSuccess {
                val user = AppConfig.userInfo.value
                if (user.img_remain_points > 0)
                    AppConfig.userInfo.value = user.copy(img_remain_points = user.img_remain_points - translateEngine.getPoint())
                translateState = LoadingState.Success(it)
            }.onFailure {
                it.printStackTrace()
                translateState = LoadingState.Failure(it)
                FunnyApplication.ctx.toastOnUi("翻译错误！原因是：${it.message}")
            }
        }
    }

    fun isTranslating() = translateJob?.isActive == true
    fun updateImageUri(uri: Uri?){ imageUri = uri }
    fun updateSourceLanguage(language: Language){ sourceLanguage = language }
    fun updateTargetLanguage(language: Language){ targetLanguage = language }
    fun updateImgSize(w: Int, h: Int){ imgWidth = w; imgHeight = h }
    fun updateTranslateEngine(new: ImageTranslationEngine){
        if (translateEngine != new){
            DataSaverUtils.saveData(translateEngine.selectKey, false)
            translateEngine = new
            DataSaverUtils.saveData(new.selectKey, true)
        }
    }

    companion object {
        private const val TAG = "ImageTransVM"
    }
}