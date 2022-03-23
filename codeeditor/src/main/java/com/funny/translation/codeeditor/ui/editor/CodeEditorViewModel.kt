package com.funny.translation.codeeditor.ui.editor

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.funny.translation.codeeditor.base.BaseViewModel
import io.github.rosemoe.editor.widget.EditorColorScheme
import io.github.rosemoe.editor.widget.schemes.*

enum class EditorSchemes(val displayName:String,val scheme: EditorColorScheme){
    LIGHT("Light",EditorColorScheme()),
    Darcula("Darcula", SchemeDarcula()),
    Eclipse("Eclipse", SchemeEclipse()),
    Github("Github",SchemeGitHub()),
    NotepadXX("Notepad++",SchemeNotepadXX()),
    VS2019("VS2019",SchemeVS2019())
}

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

    val editorColorScheme = MutableLiveData(
        EditorSchemes.LIGHT
    )

    var hasSaved = true
    val shouldUndo = MutableLiveData(false)
    val shouldRedo = MutableLiveData(false)


    /**
     * 打开文件时设为true，用于手动更新text
     */
    val textChanged = MutableLiveData(false)

    fun updateEditorColorScheme(newColorScheme: EditorSchemes){
        editorColorScheme.value = newColorScheme
        //Log.d(TAG, "updateEditorColorScheme:$newColorScheme")
    }

}