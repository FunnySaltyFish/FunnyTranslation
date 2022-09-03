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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import com.funny.translation.codeeditor.ui.Screen
import com.funny.translation.codeeditor.ui.base.ComposeSpinner
import com.funny.translation.codeeditor.ui.base.ExpandableDropdownItem
import com.funny.translation.codeeditor.ui.base.SimpleDialog
import com.funny.translation.codeeditor.vm.ActivityCodeViewModel
import com.funny.translation.helper.openUrl
import com.funny.translation.helper.readText
import com.funny.translation.helper.writeText
import com.funny.translation.js.JsEngine
import com.funny.translation.js.bean.JsBean
import com.funny.translation.translate.allLanguages
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
    val snackbarHostState = remember{ SnackbarHostState() }
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
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            activityViewModel.openFileUri = it
        } catch (e: Exception) {
            activityViewModel.codeState.value = Content("打开文件失败！${e.localizedMessage}")
        } finally {
            viewModel.textChanged.value = true
        }
    }

//    val permissionLauncher =
//        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted ->
//            if(!isGranted){
//                scope.launch {
//                    scaffoldState.snackbarHostState.showSnackbar("未获取到授权，无法写入文件")
//                }
//            }
//
//        }

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
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            saveFile(it)
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        Log.d(TAG, "ComposeCodeEditor: Finish Created file : uri:$uri")
        uri?.writeText(context, activityViewModel.exportText)
    }

    val sourceString = activityViewModel.sourceString.observeAsState()
    val sourceLanguage = activityViewModel.sourceLanguage.observeAsState()
    val targetLanguage = activityViewModel.targetLanguage.observeAsState()

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

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            CodeEditorTopBar(
                debugAction = {
                    navController.navigate(Screen.ScreenCodeRunner.route) {
                        //当底部导航导航到在非首页的页面时，执行手机的返回键 回到首页
//                        popUpTo(navController.graph.startDestinationId){saveState = true}
                        //从名字就能看出来 跟activity的启动模式中的SingleTop模式一样 避免在栈顶创建多个实例
                        launchSingleTop = true
                        //切换状态的时候保存页面状态
                        restoreState = true
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
                schemeAction = {
                    viewModel.updateEditorColorScheme(it)
                },
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
                                activityViewModel.exportText = JsBean.GSON.toJson(jsBean)
                                exportLauncher.launch("${jsBean.fileName}.json")
                                scope.launch {
                                    snackbarHostState.showSnackbar(BaseApplication.resources.getString(R.string.export_plugin_success))
                                }
                            },
                            onError = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(BaseApplication.resources.getString(R.string.export_plugin_error))
                                }
                            }
                        )

                    }
                }
            )
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(it)) {
            Editor(
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
                                value = sourceString.value!!,
                                onValueChange = { value ->
                                    activityViewModel.sourceString.value = value
                                    //JsConfig.SCRIPT_ENGINE.put("sourceString",value)
                                },
                                label = { Text(stringResource(R.string.trans_text)) },
                                placeholder = { Text(sourceString.value!!) }
                            )
                            Spacer(Modifier.height(8.dp))
                            ComposeSpinner(
                                data = activityViewModel.allLanguageNames,
                                initialData = sourceLanguage.value!!.name,
                                selectAction = { index ->
                                    activityViewModel.sourceLanguage.value = allLanguages[index]
                                    //JsConfig.SCRIPT_ENGINE.put("sourceLanguage",index)
                                },
                                label = stringResource(R.string.source_language)
                            )
                            Spacer(Modifier.height(8.dp))
                            ComposeSpinner(
                                data = activityViewModel.allLanguageNames,
                                initialData = targetLanguage.value!!.name,
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
    SmallTopAppBar(
        title = {
            Text("编辑代码")
        },
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer),
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
    val scheme = viewModel.editorColorScheme.observeAsState()
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
    viewModel: CodeEditorViewModel,
    activityViewModel: ActivityCodeViewModel
) {
    val editor = rememberCodeEditor(viewModel)
    val scheme = viewModel.editorColorScheme.observeAsState()
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

    Column {

        DisposableEffect(key1 = editor) {
            onDispose {
//                Log.d(TAG, "Editor: onDispose")
                editor.hideAutoCompleteWindow()
                editor.hideSoftInput()
            }
        }

        val textChange = viewModel.textChanged.observeAsState()
        val shouldUndo = viewModel.shouldUndo.observeAsState()
        val shouldRedo = viewModel.shouldRedo.observeAsState()
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
                .fillMaxWidth()
                .weight(if (symbolChannel == null) 100f else 95f),
            update = {
                it.colorScheme = scheme.value!!.scheme
                if (textChange.value == true) {
                    it.setText(codeText.value)
                    viewModel.textChanged.value = false
                }
                if (shouldRedo.value == true) {
                    it.redo()
                    viewModel.shouldRedo.value = false
                }
                if (shouldUndo.value == true) {
                    it.undo()
                    viewModel.shouldUndo.value = false
                }
//                Log.d(TAG, "Editor: updated! editor:$editor\n symbolChannel:$symbolChannel text:${it.text}")
//                Log.d(TAG, "Editor: rememberedTExt:$codeText")
            }
        )
        val symbols = viewModel.symbolsData.observeAsState()
        symbolChannel?.let { channel ->
            LazyRow(
                modifier = Modifier.weight(5f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(symbols.value!!) { _, item ->
                    //Log.d(TAG, "ComposeSymbolInsert: channel:${channel}")
                    ComposeSymbolInsertItem(symbolChannel = channel, symbol = item)
                }
            }
        }
    }
}