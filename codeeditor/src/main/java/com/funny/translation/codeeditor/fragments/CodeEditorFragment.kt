package com.funny.translation.codeeditor.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.funny.translation.codeeditor.R
import com.funny.translation.codeeditor.base.BaseFragment
import com.funny.translation.codeeditor.databinding.CodeEditorFragmentBinding
import com.funny.translation.codeeditor.vm.CodeEditorViewModel
import io.github.rosemoe.editor.langs.desc.JavaScriptDescription
import io.github.rosemoe.editor.langs.universal.UniversalLanguage
import io.github.rosemoe.editor.widget.CodeEditor

class CodeEditorFragment : BaseFragment(R.layout.code_editor_fragment) {
    private lateinit var codeEditorFragmentBinding: CodeEditorFragmentBinding
    private val codeEditorViewModel by viewModels<CodeEditorViewModel>()
    private val clickProxy = ClickProxy()

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        codeEditorFragmentBinding = DataBindingUtil.setContentView(requireActivity(),R.layout.code_editor_fragment)


        with(codeEditorFragmentBinding.symbolInput) {
            bindEditor(codeEditorFragmentBinding.codeEditor)
            addSymbols(codeEditorViewModel.displaySymbols,codeEditorViewModel.insertSymbols)
        }

        with(codeEditorFragmentBinding.codeEditor){
            typefaceText = Typeface.MONOSPACE
            isOverScrollEnabled = false
            setEditorLanguage(UniversalLanguage(JavaScriptDescription()))
            setNonPrintablePaintingFlags(CodeEditor.FLAG_DRAW_WHITESPACE_LEADING or CodeEditor.FLAG_DRAW_LINE_SEPARATOR)
        }

        with(codeEditorFragmentBinding){
            data = codeEditorViewModel
            click = clickProxy
        }

    }

    inner class ClickProxy(){
        fun gotoLast(view: View){}

        fun gotoNext(view: View){}

        fun replace(view: View){

        }

        fun replaceAll(view: View){

        }
    }
}