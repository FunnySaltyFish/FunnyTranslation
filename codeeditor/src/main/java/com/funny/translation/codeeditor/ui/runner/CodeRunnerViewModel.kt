package com.funny.translation.codeeditor.ui.runner

import android.app.Application
import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.funny.translation.codeeditor.base.BaseViewModel
import com.funny.translation.debug.Debug
import com.funny.translation.js.JsEngine
import com.funny.translation.js.bean.JsBean
import com.funny.translation.js.core.JsTranslateTask
import com.funny.translation.trans.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CodeRunnerViewModel(application: Application) : BaseViewModel(application),
    Debug.DebugTarget {
    private var jsEngine: JsEngine? = null
    val sourceString by lazy { MutableLiveData("") }
    val sourceLanguage by lazy { MutableLiveData(Language.CHINESE.id) }
    val targetLanguage by lazy { MutableLiveData(Language.ENGLISH.id) }

    val outputDebug = MutableLiveData("")

    fun initJs(code: String) {
        val jsBean = JsBean(999, code = code)
        //Log.d(TAG, "initJs: code:$code")
        jsEngine = JsEngine(jsBean).apply {
            loadBasicConfigurations(
                onSuccess = {
                    val jsTranslateTask = JsTranslateTask(
                        jsEngine = this,
                        sourceLanguage = sourceLanguage.value!!.toShort(),
                        targetLanguage = targetLanguage.value!!.toShort(),
                        sourceString = sourceString.value!!
                    )
                    jsTranslateTask.translate()
                },
                onError = {

                }
            )
        }
    }

    init {
        Debug.addTarget(this)
    }

    companion object{
        const val TAG = "CodeRunnerVM"
    }

    override val source: String
        get() = "插件"

    override fun appendLog(text: CharSequence) {
        viewModelScope.launch {
            withContext(Dispatchers.Main){
                outputDebug.value = "${outputDebug.value}\n$text"
            }
        }
    }
}