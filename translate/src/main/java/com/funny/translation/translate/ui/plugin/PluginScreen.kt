package com.funny.translation.translate.ui.plugin

import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.End
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.translation.helper.readText
import com.funny.translation.js.bean.JsBean
import com.funny.translation.translate.LocalSnackbarState
import com.funny.translation.translate.R
import com.funny.translation.translate.ui.widget.SimpleDialog
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch

private const val TAG = "PluginScreen"

@Composable
fun PluginScreen() {
    val vm: PluginViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarState.current

    val showSnackbar : (String) -> Unit = {
        scope.launch {
            snackbarHostState.showSnackbar(it)
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val maxW = maxWidth
        if (maxWidth > 720.dp){ //宽屏
            Row(
                Modifier
                    .fillMaxSize()
                    .padding(12.dp), horizontalArrangement = Arrangement.SpaceAround) {
                LocalPluginPart(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.45f),
                    vm = vm,
                    showSnackbar = showSnackbar
                )
                OnlinePluginPart(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(maxW * 0.45f),
                    showSnackbar = showSnackbar
                )
            }

        }else{
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(24.dp)) {
                LocalPluginPart(modifier = Modifier.fillMaxWidth(), vm = vm, showSnackbar = showSnackbar)
                OnlinePluginPart(modifier = Modifier.fillMaxWidth(), showSnackbar = showSnackbar)
            }
        }
    }


}

@Composable
fun LocalPluginPart(
    modifier: Modifier,
    vm : PluginViewModel,
    showSnackbar: (String) -> Unit
){
    val plugins by vm.plugins.collectAsState(initial = arrayListOf())

    var needToDeletePlugin: JsBean? = null
    val showDeleteDialog = remember {
        mutableStateOf(false)
    }

    var showAddPluginMenu by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

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

    SimpleDialog(
        openDialog = showDeleteDialog,
        title = stringResource(id = R.string.message_confirm),
        message = stringResource(id = R.string.message_delete_plugin),
        confirmButtonText = stringResource(R.string.message_yes),
        confirmButtonAction = {
            needToDeletePlugin?.let { vm.deletePlugin(it) }
        }
    )

    Column(
        modifier = modifier
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(id = R.string.manage_plugins),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )
            IconButton(onClick = { showAddPluginMenu = true }) {
                Icon(
                    Icons.Default.AddCircle,
                    tint = MaterialTheme.colors.onBackground,
                    contentDescription = "Add plugins"
                )
                DropdownMenu(
                    expanded = showAddPluginMenu,
                    onDismissRequest = { showAddPluginMenu = false }) {
                    DropdownMenuItem(onClick = {
                        showAddPluginMenu = false
                        importPluginLauncher.launch(arrayOf("application/javascript"))
                    }) {
                        Text(stringResource(id = R.string.import_plugin))
                    }
                    DropdownMenuItem(onClick = {
                        showAddPluginMenu = false
                        val clazz =
                            Class.forName("com.funny.translation.codeeditor.CodeEditorActivity")
                        val intent = Intent(context, clazz)
                        context.startActivity(intent)
                    }) {
                        Text(stringResource(id = R.string.create_plugin))
                    }
                }
            }
        }

        PluginList(plugins = plugins, deletePlugin = {
            showDeleteDialog.value = true
            needToDeletePlugin = it
        }, updateSelect = {
            it.enabled = 1 - it.enabled
            vm.updatePlugin(it)
        })
    }
}

@Composable
fun OnlinePluginPart(modifier: Modifier, showSnackbar: (String) -> Unit) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.online_plugin),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        OnlinePluginList(modifier = Modifier.fillMaxWidth(), showSnackbar = showSnackbar)
        //PreviewPluginList()
    }
}

@Composable
fun PluginList(
    plugins : List<JsBean>,
    updateSelect: (JsBean) -> Unit,
    deletePlugin : (JsBean)->Unit
) {
    if (plugins.isNotEmpty()) {
        LazyColumn(
            verticalArrangement = spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            modifier = Modifier.heightIn(0.dp, 360.dp)
        ) {
            itemsIndexed(plugins) { _: Int, item: JsBean ->
                PluginItem(plugin = item, updateSelect = updateSelect, deletePlugin = deletePlugin)
            }
        }
    }else{
        Text(text = "空空如也~\n去添加一个插件试试吧", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontWeight = W400, color = Color.Gray)
    }
}

@Composable
fun PluginItem(
    plugin : JsBean,
    updateSelect : (JsBean)->Unit,
    deletePlugin : (JsBean)->Unit
) {
    var selected by remember {
        mutableStateOf(plugin.enabled>0)
    }
    var expand by remember {
        mutableStateOf(false)
    }
    Column(modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(MaterialTheme.colors.surface)
        .clickable { expand = !expand }
        .animateContentSize()
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(plugin.fileName, fontSize = 18.sp, fontWeight = W600)
            Checkbox(checked = selected, onCheckedChange = {
                updateSelect(plugin)
                selected = !selected
            }, colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colors.onSurface))
        }
        if (expand){
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)) {
                MarkdownText(markdown = plugin.markdown, Modifier.padding(horizontal = 8.dp))
                Row(horizontalArrangement = End, modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)) {
                    Text(stringResource(id = R.string.delete_plugin), color = MaterialTheme.colors.onSurface, fontWeight = W600, modifier = Modifier
                        .padding(4.dp)
                        .clickable {
                            deletePlugin(plugin)
                        })
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}


private val JsBean.markdown
    get() = """
        By **${this.author}**    **v${this.version}**  
        ${this.description.replace("[Markdown]","")}  
    """.trimPreSpace.also {
        Log.d(TAG, "显示的东西:\n $it")
    }

private val String.trimPreSpace
    get() = this.split("\n").joinToString("\n") { it.trimStart() }.trimStart()