package com.funny.translation.codeeditor.vm

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.funny.translation.codeeditor.base.BaseViewModel
import com.funny.translation.codeeditor.extensions.readAssets
import io.github.rosemoe.editor.text.Content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivityCodeViewModel(application: Application) : BaseViewModel(application) {
    private val TAG = "ActivityCodeVM"

    val codeState by lazy {
        mutableStateOf(Content(_initialCode))
    }

    private val _initialCode by lazy {
        context.readAssets("js_template.js")
//        var text = ""
//        val job = viewModelScope.launch(Dispatchers.IO) {
//            Log.d(TAG, "launched")
//            text = context.readAssets("js_template.js")
//            Log.d(TAG, "finishRead . text:$text")
//        }
//
//        text
    }
}