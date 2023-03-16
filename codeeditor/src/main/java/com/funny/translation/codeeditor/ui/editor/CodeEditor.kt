@file:OptIn(ExperimentalMaterial3Api::class)

package com.funny.translation.codeeditor.ui.editor

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.funny.translation.BaseApplication
import com.funny.translation.codeeditor.R
import com.funny.translation.codeeditor.ui.base.ComposeSpinner
import com.funny.translation.codeeditor.ui.base.ExpandableDropdownItem
import com.funny.translation.codeeditor.ui.base.SimpleDialog
import com.funny.translation.codeeditor.ui.runner.CodeRunnerText
import com.funny.translation.codeeditor.ui.runner.CodeRunnerViewModel
import com.funny.translation.codeeditor.vm.ActivityCodeViewModel
import com.funny.translation.helper.JsonX
import com.funny.translation.helper.openUrl
import com.funny.translation.helper.readText
import com.funny.translation.helper.writeText
import com.funny.translation.js.JsEngine
import com.funny.translation.js.bean.JsBean
import com.funny.translation.translate.allLanguages
import com.funny.translation.translate.ui.widget.SubcomposeBottomFirstLayout
import io.github.rosemoe.editor.interfaces.EditorEventListener
import io.github.rosemoe.editor.text.Content
import io.github.rosemoe.editor.widget.CodeEditor
import kotlinx.coroutines.launch

@Composable
fun ComposeCodeEditor(
    navController: NavController,
    activityViewModel: ActivityCodeViewModel
) {
    val viewModel: CodeEditorViewModel = viewModel()
    val codeRunnerVM: CodeRunnerViewModel = viewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val confirmOpenFile = remember { mutableStateOf(false) }
    val settingArgumentsDialog = remember { mutableStateOf(false) }
    val confirmLeave = remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) {
        it ?: return@rememberLauncherForActivityResult
        try {
            val text = it.readText(context)
            activityViewModel.codeState.value = Content(text)
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            activityViewModel.openFileUri = it
        } catch (e: Exception) {
            activityViewModel.codeState.value = Content("打开文件失败！${e.localizedMessage}")
        } finally {
            viewModel.textChanged.value = true
        }
    }

    fun saveFile(uri: Uri) {
        try {
            uri.writeText(context, activityViewModel.codeState.value.toString())
            viewModel.hasSaved = true
            scope.launch { snackbarHostState.showSnackbar("保存完成") }
        } catch (e: Exception) {
            e.printStackTrace()
            scope.launch { snackbarHostState.showSnackbar("发生错误，保存失败！") }
        }
    }

    val fileCreatorLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/javascript"),
    ) { uri ->
        Log.d(TAG, "ComposeCodeEditor: Finish Created file : uri:$uri")
        uri?.let {
            activityViewModel.openFileUri = it
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            saveFile(it)
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        Log.d(TAG, "ComposeCodeEditor: Finish Created file : uri:$uri")
        uri?.writeText(context, activityViewModel.exportText)
    }

    val sourceString = activityViewModel.sourceString
    val sourceLanguage = activityViewModel.sourceLanguage
    val targetLanguage = activityViewModel.targetLanguage

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    fun finish() {
        (context as ComponentActivity).finish()
    }

    BackHandler(enabled = navController.previousBackStackEntry == null) {
        if (!viewModel.hasSaved) {
            confirmLeave.value = true
        } else {
            finish()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SelectionContainer(modifier = Modifier
                .width(300.dp)
                .fillMaxHeight(1f)
                .background(
                    MaterialTheme.colorScheme.primaryContainer,
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )) {
                CodeRunnerText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 16.dp, end = 4.dp)
                        .verticalScroll(rememberScrollState()),
                    viewModel = codeRunnerVM,
                    activityCodeViewModel = activityViewModel
                )
            }
        },
        modifier = Modifier.fillMaxSize(),
        gesturesEnabled = drawerState.isOpen
    ) {
        Scaffold(
            Modifier.fillMaxSize(),
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                CodeEditorTopBar(
                    debugAction = {
                        activityViewModel.shouldExecuteCode.value = true
                        if (drawerState.isClosed)
                            scope.launch {
                                drawerState.open()
                            }
                    },
                    saveAction = {
                        //permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        //如果当前打开的是默认文件
                        if (activityViewModel.openFileUri.encodedPath.isNullOrBlank()) {
                            fileCreatorLauncher.launch("new_plugin_${System.currentTimeMillis()}.js")
                        } else { //已经打开了文件
                            if (!viewModel.hasSaved) {
                                saveFile(uri = activityViewModel.openFileUri)
                            }
                        }
                    },
                    undoAction = {
                        viewModel.shouldUndo.value = true
                    },
                    redoAction = {
                        viewModel.shouldRedo.value = true
                    },
                    schemeAction = viewModel::updateEditorColorScheme,
                    openFileAction = {
                        if (!viewModel.hasSaved) {
                            confirmOpenFile.value = true
                        } else {
                            filePickerLauncher.launch(arrayOf("application/javascript"))
                        }
                    },
                    setArgumentsAction = {
                        settingArgumentsDialog.value = true
                    },
                    openPluginDocumentAction = {
                        context.openUrl("https://www.yuque.com/funnysaltyfish/vzmuud")
                    },
                    exportAction = {
                        val jsBean = JsBean()
                        jsBean.code = activityViewModel.codeState.value.toString()
                        val jsEngine = JsEngine(jsBean)
                        scope.launch {
                            jsEngine.loadBasicConfigurations(
                                onSuccess = {
                                    activityViewModel.exportText = JsonX.toJsonPretty(jsBean)
                                    exportLauncher.launch("${jsBean.fileName}.json")
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            BaseApplication.resources.getString(
                                                R.string.export_plugin_success
                                            )
                                        )
                                    }
                                },
                                onError = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            BaseApplication.resources.getString(
                                                R.string.export_plugin_error
                                            )
                                        )
                                    }
                                }
                            )

                        }
                    }
                )
            }
        ) {
            Editor(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                viewModel = viewModel,
                activityViewModel = activityViewModel
            )

            SimpleDialog(
                openDialog = confirmOpenFile,
                title = stringResource(R.string.message_hint),
                message = stringResource(R.string.message_open_while_not_saved),
                confirmButtonAction = {
                    filePickerLauncher.launch(
                        arrayOf(
                            "application/javascript",
                        )
                    )
                }
            )

            SimpleDialog(
                openDialog = confirmLeave,
                title = "提示",
                message = stringResource(R.string.message_leave_not_saved),
                confirmButtonAction = {
                    finish()
                })

            if (settingArgumentsDialog.value) {
                AlertDialog(
                    onDismissRequest = { },
                    title = {
                        Text(text = stringResource(id = R.string.change_debug_args))
                    },
                    text = {
                        Column {
                            TextField(
                                value = sourceString.value,
                                onValueChange = { value ->
                                    activityViewModel.sourceString.value = value
                                    //JsConfig.SCRIPT_ENGINE.put("sourceString",value)
                                },
                                label = { Text(stringResource(R.string.trans_text)) },
                                placeholder = { Text(sourceString.value) }
                            )
                            Spacer(Modifier.height(8.dp))
                            ComposeSpinner(
                                data = activityViewModel.allLanguageNames,
                                initialData = sourceLanguage.value.name,
                                selectAction = { index ->
                                    activityViewModel.sourceLanguage.value = allLanguages[index]
                                    //JsConfig.SCRIPT_ENGINE.put("sourceLanguage",index)
                                },
                                label = stringResource(R.string.source_language)
                            )
                            Spacer(Modifier.height(8.dp))
                            ComposeSpinner(
                                data = activityViewModel.allLanguageNames,
                                initialData = targetLanguage.value.name,
                                selectAction = { index ->
                                    activityViewModel.targetLanguage.value = allLanguages[index]
                                    //JsConfig.SCRIPT_ENGINE.put("targetLanguage",index)
                                },
                                label = stringResource(R.string.target_language)
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = { settingArgumentsDialog.value = false }) {
                            Text(text = stringResource(R.string.close))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CodeEditorTopBar(
    debugAction: () -> Unit,
    saveAction: () -> Unit,
    undoAction: () -> Unit,
    redoAction: () -> Unit,
    schemeAction: (EditorSchemes) -> Unit,
    setArgumentsAction: () -> Unit,
    openFileAction: () -> Unit,
    openPluginDocumentAction: () -> Unit,
    exportAction: () -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    TopAppBar(
        title = {
            Text("编辑代码")
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer),
        actions = {
            IconButton(onClick = debugAction) {
                Icon(painterResource(id = R.drawable.ic_debug), contentDescription = "Debug")
            }
            IconButton(onClick = saveAction) {
                Icon(painterResource(id = R.drawable.ic_save), contentDescription = "Save")
            }
            IconButton(onClick = { expanded = true }) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = "More"
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(onClick = {
                    undoAction()
                }, text = {
                    Text(text = stringResource(id = R.string.undo))
                })
                DropdownMenuItem(onClick = {
                    redoAction()
                }, text = {
                    Text(text = stringResource(id = R.string.redo))
                })
                DropdownMenuItem(onClick = {
                    openFileAction()
                    expanded = false
                }, text = {
                    Text(text = stringResource(id = R.string.open_file))
                })
                DropdownMenuItem(onClick = {
                    exportAction()
                    expanded = false
                }, text = {
                    Text(stringResource(id = R.string.export_plugin))
                })
                ExpandableDropdownItem(
                    stringResource(id = R.string.change_editor_theme),
                    requestDismiss = {
                        //expanded = false}
                    }) {
                    for (editorScheme in EditorSchemes.values()) {
                        DropdownMenuItem(onClick = {
                            schemeAction(editorScheme)
                            expanded = false
                        }, text = {
                            Text(text = editorScheme.displayName)
                        })
                    }
                }
                DropdownMenuItem(onClick = {
                    setArgumentsAction()
                    expanded = false
                }, text = {
                    Text(text = stringResource(id = R.string.set_debug_arguments))
                })
                DropdownMenuItem(onClick = {
                    openPluginDocumentAction()
                    expanded = false
                }, text = {
                    Text(text = stringResource(id = R.string.open_plugin_document))
                })
            }
        }
    )
}


@Composable
fun rememberCodeEditor(viewModel: CodeEditorViewModel): CodeEditor {
    val ctx = LocalContext.current
    val scheme = viewModel.editorColorScheme
    return remember(scheme) {
        Log.d(TAG, "rememberCodeEditor: ${scheme.value}")
        CodeEditor(ctx).apply {
            typefaceText = Typeface.MONOSPACE
            isOverScrollEnabled = false

            setEditorLanguage(PluginLanguage(FunnyPluginDescription))
            setNonPrintablePaintingFlags(CodeEditor.FLAG_DRAW_WHITESPACE_LEADING or CodeEditor.FLAG_DRAW_LINE_SEPARATOR)
            //colorScheme = scheme.value!!.scheme
        }
    }
}

@Composable
fun Editor(
    modifier: Modifier,
    viewModel: CodeEditorViewModel,
    activityViewModel: ActivityCodeViewModel
) {
    val editor = rememberCodeEditor(viewModel)
    val scheme = viewModel.editorColorScheme
    val symbolChannel = remember {
        editor.createNewSymbolChannel()
    }
    val codeText = activityViewModel.codeState

    fun updateEditorText() {
        editor.text.let {
            codeText.value = it
        }
        viewModel.hasSaved = false
    }

    SubcomposeBottomFirstLayout(modifier, bottom = {
        val symbols = viewModel.symbolsData
        symbolChannel?.let { channel ->
            LazyRow(
                modifier = Modifier
                    .padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(symbols) { _, item ->
                    //Log.d(TAG, "ComposeSymbolInsert: channel:${channel}")
                    ComposeSymbolInsertItem(symbolChannel = channel, symbol = item)
                }
            }
        }
    }) {
        DisposableEffect(key1 = editor) {
            onDispose {
                editor.hideAutoCompleteWindow()
                editor.hideSoftInput()
            }
        }

        val textChange = viewModel.textChanged
        val shouldUndo = viewModel.shouldUndo
        val shouldRedo = viewModel.shouldRedo
        AndroidView(
            factory = {
                editor.apply {
                    setText(codeText.value)
                    //colorScheme = scheme.value!!.scheme
                    setEventListener(object : EditorEventListener {
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
                            updateEditorText()
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
                            updateEditorText()
                        }

                        override fun beforeReplace(editor: CodeEditor?, content: CharSequence?) {}
                    })
                }
            },
            modifier = Modifier
                .fillMaxSize(),
            update = {
                it.colorScheme = scheme.value.scheme
                if (textChange.value) {
                    it.setText(codeText.value)
                    viewModel.textChanged.value = false
                }
                if (shouldRedo.value) {
                    it.redo()
                    viewModel.shouldRedo.value = false
                }
                if (shouldUndo.value) {
                    it.undo()
                    viewModel.shouldUndo.value = false
                }
            }
        )
    }
}