package com.funny.translation.codeeditor.ui.editor

import android.graphics.Typeface
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.funny.translation.codeeditor.R
import com.funny.translation.codeeditor.extensions.getActivity
import com.funny.translation.codeeditor.ui.Screen
import com.funny.translation.codeeditor.vm.ActivityCodeViewModel
import io.github.rosemoe.editor.interfaces.EditorEventListener
import io.github.rosemoe.editor.langs.desc.JavaScriptDescription
import io.github.rosemoe.editor.langs.universal.UniversalLanguage
import io.github.rosemoe.editor.text.Content
import io.github.rosemoe.editor.widget.CodeEditor

@Composable
fun ComposeCodeEditor(
    navController: NavController,
    activityViewModel: ActivityCodeViewModel
){
    val viewModel : CodeEditorViewModel = viewModel()
    Scaffold(
        topBar = {
            ComposeTopBar(
                debugAction = {
                    navController.navigate(Screen.ScreenCodeRunner.route){
                        launchSingleTop = true
                    }
                }
            )
        },
        modifier = Modifier.fillMaxWidth()
    ){
        Editor(
            viewModel = viewModel,
            activityViewModel = activityViewModel
        )
    }
}

@Composable
fun ComposeTopBar(
    debugAction : ()->Unit
){
    TopAppBar(
        title = {
            Text("你好")
        },
        modifier = Modifier.fillMaxWidth(),
        actions = {
            IconButton(onClick = debugAction) {
                Icon(painterResource(id = R.drawable.ic_debug),contentDescription = "Debug")
            }
        }
    )
}

@Composable
fun rememberCodeEditor(): CodeEditor {
    val ctx = LocalContext.current
    return remember(

    ) {
        CodeEditor(ctx).apply {
            typefaceText = Typeface.MONOSPACE
            isOverScrollEnabled = false
            setEditorLanguage(UniversalLanguage(JavaScriptDescription()))
            setNonPrintablePaintingFlags(CodeEditor.FLAG_DRAW_WHITESPACE_LEADING or CodeEditor.FLAG_DRAW_LINE_SEPARATOR)
        }
    }
}

@Composable
fun Editor(
    viewModel: CodeEditorViewModel,
    activityViewModel : ActivityCodeViewModel
){
    Column {
        val editor = rememberCodeEditor()
        val symbolChannel = remember{
            editor.createNewSymbolChannel()
        }
        val codeText = activityViewModel.codeState
        AndroidView(
            factory = {
                editor.apply {
                    setText(codeText.value)
                    setEventListener(object : EditorEventListener{
                        override fun onRequestFormat(editor: CodeEditor?, async: Boolean) = true

                        override fun onFormatFail(editor: CodeEditor?, cause: Throwable?) = false

                        override fun onFormatSucceed(editor: CodeEditor?) {}

                        override fun onNewTextSet(editor: CodeEditor?) {}

                        override fun afterDelete(
                            editor: CodeEditor?,
                            content: CharSequence?,
                            startLine: Int,
                            startColumn: Int,
                            endLine: Int,
                            endColumn: Int,
                            deletedContent: CharSequence?
                        ) {
                            //Log.d(TAG, "afterDelete:")
                            editor?.text?.let {
                                codeText.value = it
                            }
                        }

                        override fun afterInsert(
                            editor: CodeEditor?,
                            content: CharSequence?,
                            startLine: Int,
                            startColumn: Int,
                            endLine: Int,
                            endColumn: Int,
                            insertedContent: CharSequence?
                        ) {
                            //Log.d(TAG, "afterInsert:")
                            editor?.text?.let {
                                codeText.value = it
                            }
                        }

                        override fun beforeReplace(editor: CodeEditor?, content: CharSequence?) {}
                    })
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (symbolChannel == null) 100f else 95f),
            update = {
//                Log.d(TAG, "Editor: updated! editor:$editor\n symbolChannel:$symbolChannel text:${it.text}")
//                Log.d(TAG, "Editor: rememberedTExt:$codeText")
            }
        )
        val symbols = viewModel.symbolsData.observeAsState()
        symbolChannel?.let{ channel ->
            LazyRow(
                modifier = Modifier.weight(5f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(symbols.value!!){ _, item ->
                    //Log.d(TAG, "ComposeSymbolInsert: channel:${channel}")
                    ComposeSymbolInsertItem(symbolChannel = channel, symbol = item)
                }
            }
        }
    }
}