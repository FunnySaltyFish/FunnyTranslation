package com.funny.translation.codeeditor.ui.editor

import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
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
import com.funny.translation.codeeditor.R
import com.funny.translation.codeeditor.ui.Screen
import com.funny.translation.codeeditor.ui.base.ComposeSpinner
import com.funny.translation.codeeditor.ui.base.ExpandableDropdownItem
import com.funny.translation.codeeditor.ui.base.SimpleDialog
import com.funny.translation.codeeditor.vm.ActivityCodeViewModel
import com.funny.translation.helper.openUrl
import com.funny.translation.helper.readText
import com.funny.translation.helper.writeText
import com.funny.translation.trans.allLanguages
import com.funny.translation.trans.findLanguageById
import io.github.rosemoe.editor.interfaces.EditorEventListener
import io.github.rosemoe.editor.langs.desc.JavaScriptDescription
import io.github.rosemoe.editor.langs.universal.UniversalLanguage
import io.github.rosemoe.editor.text.Content
import io.github.rosemoe.editor.widget.CodeEditor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ComposeCodeEditor(
    navController: NavController,
    activityViewModel: ActivityCodeViewModel
) {
    val viewModel: CodeEditorViewModel = viewModel()
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    val confirmOpenFile = remember { mutableStateOf(false) }
    val settingArgumentsDialog = remember { mutableStateOf(false) }
    val confirmLeave = remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) {
        if (it == null) return@rememberLauncherForActivityResult
        try {
            val text = it.readText(context)
            activityViewModel.codeState.value = Content(text)
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

    suspend fun saveFile(uri: Uri){
            try {
                withContext(Dispatchers.IO) {
                    uri.writeText(
                        context,
                        activityViewModel.codeState.value.toString()
                    )
                }
                activityViewModel.openFileUri = uri
                viewModel.hasSaved = true
                scaffoldState.snackbarHostState.showSnackbar("保存完成")
            } catch (e: Exception) {
                scaffoldState.snackbarHostState.showSnackbar("发生错误，保存失败！")
            }
    }

    val fileCreatorLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(),
    ) { uri ->
        Log.d(TAG, "ComposeCodeEditor: Finish Created file : uri:$uri")
        uri?.let {
            scope.launch {
                saveFile(it)
            }
        }
    }

    val sourceString   = activityViewModel.sourceString.observeAsState()
    val sourceLanguage = activityViewModel.sourceLanguage.observeAsState()
    val targetLanguage = activityViewModel.targetLanguage.observeAsState()

    fun finish(){
        (context as ComponentActivity).finish()
    }

    BackHandler(enabled = navController.previousBackStackEntry == null) {
        if(!viewModel.hasSaved){
            confirmLeave.value = true
        }else{
            finish()
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
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
                    if(activityViewModel.openFileUri.encodedPath.isNullOrBlank()){
                        fileCreatorLauncher.launch("new_plugin_${System.currentTimeMillis()}.js")
                    } else { //已经打开了文件
                        if(!viewModel.hasSaved){
                            scope.launch {
                                saveFile(uri = activityViewModel.openFileUri)
                            }
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
            )
        },
        modifier = Modifier.fillMaxWidth(),

        ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Editor(
                viewModel = viewModel,
                activityViewModel = activityViewModel
            )

            SimpleDialog(
                openDialog = confirmOpenFile,
                title = "提示",
                message = "您确定要打开文件吗？当前操作尚未保存……",
                confirmButtonAction = {
                    filePickerLauncher.launch(
                        arrayOf(
                            "application/javascript",
                        )
                    )
                }
            )

            SimpleDialog(openDialog = confirmLeave, title ="提示",message = "当前文件尚未保存，您确定要离开吗？",confirmButtonAction = {
                finish()
            })

            if(settingArgumentsDialog.value){
                AlertDialog(
                    onDismissRequest = {  },
                    title = {
                        Text(
                            text = "标题"
                        )
                    },
                    text = {
                        Column {
                            TextField(
                                value = sourceString.value!!,
                                onValueChange = { value ->
                                    activityViewModel.sourceString.value = value
                                    //JsConfig.SCRIPT_ENGINE.put("sourceString",value)
                                },
                                label = { Text("翻译文本") },
                                placeholder = { Text( sourceString.value!!) }
                            )
                            Spacer(Modifier.height(8.dp))
                            ComposeSpinner(
                                data = activityViewModel.allLanguageNames,
                                initialData = sourceLanguage.value!!.name,
                                selectAction = { index ->
                                    activityViewModel.sourceLanguage.value = allLanguages[index]
                                    //JsConfig.SCRIPT_ENGINE.put("sourceLanguage",index)
                                },
                                label = "源语言"
                            )
                            Spacer(Modifier.height(8.dp))
                            ComposeSpinner(
                                data = activityViewModel.allLanguageNames,
                                initialData = targetLanguage.value!!.name,
                                selectAction = { index ->
                                    activityViewModel.targetLanguage.value = allLanguages[index]
                                    //JsConfig.SCRIPT_ENGINE.put("targetLanguage",index)
                                },
                                label = "目标语言"
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = { settingArgumentsDialog.value = false }) {
                            Text(text = "关闭")

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
    setArgumentsAction : () -> Unit,
    openFileAction: () -> Unit,
    openPluginDocumentAction : ()->Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    TopAppBar(
        title = {
            Text("编辑代码")
        },
        modifier = Modifier.fillMaxWidth(),
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
                }) {
                    Text(text = stringResource(id = R.string.undo))
                }
                DropdownMenuItem(onClick = {
                    redoAction()
                }) {
                    Text(text = stringResource(id = R.string.redo))
                }
                DropdownMenuItem(onClick = {
                    openFileAction()
                    expanded = false
                }) {
                    Text(text = stringResource(id = R.string.open_file))
                }
                ExpandableDropdownItem("更改主题", requestDismiss = {
                    //expanded = false}
                }) {
                    for (editorScheme in EditorSchemes.values()) {
                        DropdownMenuItem(onClick = {
                            schemeAction(editorScheme)
                            expanded = false
                        }) {
                            Text(text = editorScheme.displayName)
                        }
                    }
                }
                DropdownMenuItem(onClick = {
                    setArgumentsAction()
                    expanded = false
                }) {
                    Text(text = stringResource(id = R.string.set_debug_arguments))
                }
                DropdownMenuItem(onClick = {
                    openPluginDocumentAction()
                    expanded = false
                }) {
                    Text(text = stringResource(id = R.string.open_plugin_document))
                }
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
            setEditorLanguage(UniversalLanguage(JavaScriptDescription()))
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

    fun updateEditorText(){
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
                if(shouldRedo.value == true){
                    it.redo()
                    viewModel.shouldRedo.value = false
                }
                if(shouldUndo.value == true){
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