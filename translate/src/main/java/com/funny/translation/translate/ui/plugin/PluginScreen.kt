@file:OptIn(ExperimentalFoundationApi::class)

package com.funny.translation.translate.ui.plugin

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.compose.loading.loadingList
import com.funny.compose.loading.rememberRetryableLoadingState
import com.funny.translation.codeeditor.CodeEditorActivity
import com.funny.translation.helper.readText
import com.funny.translation.js.bean.JsBean
import com.funny.translation.translate.LocalNavController
import com.funny.translation.translate.LocalSnackbarState
import com.funny.translation.translate.R
import com.funny.translation.WebViewActivity
import com.funny.translation.translate.ui.widget.*
import com.funny.translation.ui.MarkdownText
import com.funny.translation.ui.touchToScale
import kotlinx.coroutines.launch

private const val TAG = "PluginScreen"

@Composable
fun PluginScreen() {
    val vm: PluginViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarState.current

    val showSnackbar: (String) -> Unit = {
        scope.launch {
            snackbarHostState.showSnackbar(it)
        }
    }

    val showDeleteDialogState = remember { mutableStateOf(false) }
    var showAddPluginMenu by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val localPlugins by vm.plugins.collectAsState(initial = arrayListOf())

    val importPluginLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) {
        if (it == null) return@rememberLauncherForActivityResult
        try {
            val text = it.readText(context)
            vm.importPlugin(text, successCall = { str ->
                showSnackbar(str)
            }, failureCall = { str ->
                showSnackbar(str)
            })
        } catch (e: Exception) {
            showSnackbar("加载插件出错!")
        }
    }

    val showDeleteDialogAction = remember {
        { plugin: JsBean ->
            showDeleteDialogState.value = true
            vm.needToDeletePlugin = plugin
        }
    }

    SimpleDialog(
        openDialogState = showDeleteDialogState,
        title = stringResource(id = R.string.message_confirm),
        message = stringResource(id = R.string.message_delete_plugin),
        confirmButtonText = stringResource(R.string.message_yes),
        confirmButtonAction = {
            vm.needToDeletePlugin?.let(vm::deletePlugin)
        }
    )

    var expandLocalPlugins by remember { mutableStateOf(false) }
    val localPluginPartWrapper: LazyListScope.() -> Unit = remember {
        {
            expandableStickyRow(
                titleId = R.string.local_plugins,
                expandLocalPlugins,
                { expandLocalPlugins = it }
            ) {
                localPlugins(
                    plugins = localPlugins,
                    deletePlugin = showDeleteDialogAction,
                    updateSelect = vm::updateLocalPluginSelect
                )
            }
        }
    }

    val (onlinePluginLoadingState, retryLoadOnlinePlugin) = rememberRetryableLoadingState(loader = vm::getOnlinePlugins)
    var expandOnlinePlugins by remember { mutableStateOf(true) }
    val onlinePluginListWrapper: LazyListScope.() -> Unit = remember {
        {
            expandableStickyRow(
                titleId = R.string.online_plugin,
                expandOnlinePlugins,
                { expandOnlinePlugins = it }) {
                loadingList(
                    onlinePluginLoadingState,
                    retryLoadOnlinePlugin,
                    key = { it.fileName }) { jsBean ->
                    var onlinePluginState by rememberSaveable {
                        vm.checkPluginState(jsBean)
                    }
                    OnlinePluginItem(
                        plugin = jsBean,
                        onlinePluginState = onlinePluginState,
                        clickOnlinePluginAction = object : ClickOnlinePluginAction {
                            override fun install(jsBean: JsBean) {
                                vm.installOrUpdatePlugin(jsBean, {
                                    onlinePluginState = OnlinePluginState.Installed
                                    showSnackbar(it)
                                }, {
                                    showSnackbar(it)
                                })
                            }

                            override fun delete(jsBean: JsBean) {
                                vm.deletePlugin(jsBean)
                                onlinePluginState = OnlinePluginState.NotInstalled
                            }

                            override fun update(jsBean: JsBean) {
                                vm.updatePlugin(jsBean)
                                onlinePluginState = OnlinePluginState.Installed
                            }
                        }
                    )
                }
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        PluginScreenTopBar(
            showAddPluginMenu = showAddPluginMenu,
            updateShowAddPluginMenu = { showAddPluginMenu = it },
            importPluginAction = { importPluginLauncher.launch(arrayOf("application/javascript")) },
            newFileAction = {
                ContextCompat.startActivity(
                    context,
                    Intent(context, CodeEditorActivity::class.java),
                    null
                )
            },
        )

        BoxWithConstraints(Modifier.fillMaxSize()) {
            if (maxWidth > 720.dp) { //宽屏
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp), horizontalArrangement = Arrangement.SpaceAround
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.45f),
                        verticalArrangement = spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        localPluginPartWrapper()
                    }
                    Spacer(modifier = Modifier.weight(0.015f))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.45f),
                        verticalArrangement = spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        onlinePluginListWrapper()
                    }
                }

            } else {
                val lazyListState = rememberSaveable(saver = LazyListState.Saver) {
                    LazyListState()
                }

                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(start = 20.dp, end = 20.dp),
                    verticalArrangement = spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                    state = lazyListState
                ) {
                    localPluginPartWrapper()
                    item {
                        Divider()
                    }
                    onlinePluginListWrapper()
                }
            }
        }
    }
}

fun LazyListScope.expandableStickyRow(
    titleId: Int,
    expand: Boolean,
    updateExpand: (Boolean) -> Unit,
    contentList: LazyListScope.() -> Unit
) {
    stickyHeader {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { updateExpand(!expand) }
                .background(MaterialTheme.colorScheme.background)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(id = titleId),
                fontWeight = W600,
                fontSize = 24.sp
            )
            ExpandMoreButton(expand = expand, onClick = {
                updateExpand(!expand)
            })
        }
    }

    if (expand) contentList()
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PluginScreenTopBar(
    showAddPluginMenu: Boolean,
    updateShowAddPluginMenu: (Boolean) -> Unit,
    importPluginAction: () -> Unit,
    newFileAction: () -> Unit
) {
    val navController = LocalNavController.current
    TopAppBar(
        modifier = Modifier.padding(end = 18.dp),
        title = {
            Text(text = stringResource(id = R.string.manage_plugins))
        },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    tint = MaterialTheme.colorScheme.onBackground,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            IconButton(onClick = { updateShowAddPluginMenu(true) }) {
                Icon(
                    Icons.Default.AddCircle,
                    tint = MaterialTheme.colorScheme.onBackground,
                    contentDescription = "Add plugins"
                )
                DropdownMenu(
                    expanded = showAddPluginMenu,
                    onDismissRequest = { updateShowAddPluginMenu(false) }) {
                    DropdownMenuItem(onClick = {
                        updateShowAddPluginMenu(false)
                        importPluginAction()
                    }, text = {
                        Text(stringResource(id = R.string.import_plugin))
                    })
                    DropdownMenuItem(onClick = {
                        updateShowAddPluginMenu(false)
                        newFileAction()
                    }, text = {
                        Text(stringResource(id = R.string.create_plugin))
                    })
                }
            }
        }
    )
}


private fun LazyListScope.localPlugins(
    plugins: List<JsBean>,
    updateSelect: (JsBean) -> Unit,
    deletePlugin: (JsBean) -> Unit
) {
    if (plugins.isNotEmpty()) {
        itemsIndexed(plugins) { _: Int, item: JsBean ->
            PluginItem(plugin = item, updateSelect = updateSelect, deletePlugin = deletePlugin)
        }
    } else {
        item {
            Text(
                text = stringResource(id = R.string.empty_plugin_tip),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = W400,
                color = Color.Gray
            )
        }
    }
}


@Composable
internal fun PluginItem(
    plugin: JsBean,
    updateSelect: (JsBean) -> Unit,
    deletePlugin: (JsBean) -> Unit
) {
    var expand by remember {
        mutableStateOf(false)
    }
    var selected by remember {
        mutableStateOf(plugin.enabled > 0)
    }
    val context = LocalContext.current
    Column(modifier = Modifier
        .touchToScale { expand = !expand }
        .fillMaxWidth()
        .clip(RoundedCornerShape(28.dp))
        .background(MaterialTheme.colorScheme.primaryContainer)
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            horizontalArrangement = SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(plugin.fileName, fontSize = 22.sp, fontWeight = W600)
            Switch(checked = selected, onCheckedChange = {
                updateSelect(plugin)
                selected = !selected
            })
        }
        MarkdownText(
            markdown = plugin.markdown,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.9f),
            maxLines = if (expand) Int.MAX_VALUE else 1,
            onLinkClicked = { context, url ->
                WebViewActivity.start(context, url)
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (expand) {
            Row(horizontalArrangement = SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.padding(bottom = 4.dp),
                    text = stringResource(R.string.plugin_info_template, plugin.author, plugin.version),
                    fontWeight = W600
                )
                Text(
                    stringResource(id = R.string.delete_plugin),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = W600,
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable {
                            deletePlugin(plugin)
                        }
                )
            }
        }
    }
}