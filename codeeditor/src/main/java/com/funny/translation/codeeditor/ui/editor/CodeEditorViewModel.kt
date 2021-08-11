package com.funny.translation.codeeditor.ui.editor

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.funny.translation.codeeditor.base.BaseViewModel

class CodeEditorViewModel(application: Application) : BaseViewModel(application) {
    private val _symbols by lazy{
        val shows =
            arrayOf("->", "=", "{", "}", "(", ")", ",", ".", ";", "\"", "?", "+", "-", "*", "/")
        val inserts =
            arrayOf("\t", "=", "{}", "}", "(", ")", ",", ".", ";", "\"", "?", "+", "-", "*", "/")
        (shows.indices).map { i ->
            Symbol(shows[i],inserts[i])
        }
    }

    val symbolsData by lazy {
        MutableLiveData(_symbols)
    }

}