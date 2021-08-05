package com.funny.translation.codeeditor.vm

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.funny.translation.codeeditor.base.BaseViewModel
import com.funny.translation.codeeditor.extensions.readAssets

class CodeEditorViewModel(application: Application) : BaseViewModel(application) {
    lateinit var code : MutableLiveData<String>
    val displaySymbols = arrayOf("->","=", "{", "}", "(", ")", ",", ".", ";", "\"", "?", "+", "-", "*", "/")
    val insertSymbols = arrayOf("\t", "=","{}", "}", "(", ")", ",", ".", ";", "\"", "?", "+", "-", "*", "/")

    fun setCode(newCode:String){
        code.value = newCode
    }

    init {
        execute {
            code = MutableLiveData<String>(context.readAssets("js_template.js"))
        }
    }

}