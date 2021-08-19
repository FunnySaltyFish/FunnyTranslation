package com.funny.translation.codeeditor.vm

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import com.funny.translation.codeeditor.base.BaseViewModel
import com.funny.translation.codeeditor.extensions.readAssets
import com.funny.translation.helper.DataStoreUtils
import com.funny.translation.helper.readText
import com.funny.translation.js.config.DataStoreConfig
import com.funny.translation.trans.Language
import io.github.rosemoe.editor.text.Content

class ActivityCodeViewModel(application: Application) : BaseViewModel(application) {
    private val TAG = "ActivityCodeVM"

    val codeState by lazy {
        mutableStateOf(Content(_initialCode))
    }

    private val _initialCode by lazy {
        try{
            //val path = openFileUri.encodedPath
            //if(path.isNullOrEmpty())
            context.readAssets("js_template.js")
            //else openFileUri.readText(context)
        }catch (e : Exception){
            "读取文件失败！${e.message}"
        }

//        var text = ""
//        val job = viewModelScope.launch(Dispatchers.IO) {
//            Log.d(TAG, "launched")
//            text = context.readAssets("js_template.js")
//            Log.d(TAG, "finishRead . text:$text")
//        }
//
//        text
    }

    var openFileUri: Uri =
        Uri.parse("")


    val sourceString = MutableLiveData("你好")
    val sourceLanguage = MutableLiveData(Language.CHINESE.id)
    val targetLanguage = MutableLiveData(Language.ENGLISH.id)

    val allLanguageNames : List<String>
        get() {
            return Language.values().map { it.name }
        }

}