package com.funny.translation.codeeditor.vm

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.funny.translation.codeeditor.base.BaseViewModel
import com.funny.translation.helper.lazyPromise
import com.funny.translation.helper.readAssets
import com.funny.translation.translate.Language
import io.github.rosemoe.editor.text.Content
import kotlinx.coroutines.launch

class ActivityCodeViewModel(application: Application) : BaseViewModel(application) {
    private val TAG = "ActivityCodeVM"

    val codeState by lazy {
        mutableStateOf(Content(""))
    }

    var shouldExecuteCode = mutableStateOf(false)

    private val _initialCode by lazyPromise(viewModelScope) {
        try{
            context.readAssets("js_template.js")
        }catch (e : Exception){
            "读取文件失败！${e.message}"
        }
    }

    var openFileUri: Uri = Uri.parse("")

    val sourceString   = mutableStateOf("你好")
    val sourceLanguage = mutableStateOf(Language.CHINESE)
    val targetLanguage = mutableStateOf(Language.ENGLISH)

    var exportText = ""

    val allLanguageNames : List<String>
        get() {
            return Language.values().map { it.name }
        }

    private fun updateCode(str : String){
        codeState.value = Content(str)
    }

    init {
        viewModelScope.launch {
            updateCode(_initialCode.await())
        }
    }
}