package com.funny.translation.translate.ui.plugin

import android.service.autofill.OnClickAction
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.cmaterialcolors.MaterialColors
import com.funny.translation.js.bean.JsBean
import com.funny.translation.translate.R
import com.funny.translation.translate.ui.widget.LoadingContent
import dev.jeziellago.compose.markdowntext.MarkdownText


private const val TAG = "OnlinePlugin"

enum class OnlinePluginState {
    Installed, OutDated, NotInstalled
}

interface ClickPluginAction {
    fun install(jsBean: JsBean)
    fun delete(jsBean: JsBean)
    fun update(jsBean: JsBean)
}

@Composable
fun OnlinePluginList(

) {
    val vm : PluginViewModel = viewModel()

    LoadingContent(loader = vm.pluginService::getOnlinePlugins) { pluginList ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(pluginList){ _, item : JsBean ->
                var onlinePluginState by remember {
                    vm.checkPluginState(item)
                }
                OnlinePluginItem(plugin = item, onlinePluginState = onlinePluginState, clickPluginAction = object : ClickPluginAction{
                    override fun install(jsBean: JsBean) {
                        vm.installOnlinePlugin(jsBean)
                        onlinePluginState = OnlinePluginState.Installed
                    }

                    override fun delete(jsBean: JsBean) {
                        vm.deletePlugin(jsBean)
                        onlinePluginState = OnlinePluginState.NotInstalled
                    }

                    override fun update(jsBean: JsBean) {
                        vm.updatePlugin(jsBean)
                        onlinePluginState = OnlinePluginState.Installed
                    }
                })
            }
            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

@Composable
fun OnlinePluginItem(
    plugin: JsBean,
    onlinePluginState: OnlinePluginState,
    clickPluginAction: ClickPluginAction
) {
    var expand by remember {
        mutableStateOf(true)
    }
    Column(modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(MaterialTheme.colors.surface)
        .clickable { expand = !expand }
        .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(plugin.fileName, fontSize = 18.sp, fontWeight = FontWeight.W600)
            when(onlinePluginState){
                OnlinePluginState.Installed -> Button(shape = CircleShape, colors = buttonColors(
                    contentColor = MaterialTheme.colors.onBackground,
                    backgroundColor = MaterialColors.Red700
                ) ,onClick = { clickPluginAction.delete(plugin) }) {
                    Text(stringResource(R.string.uninstall_plugin))
                }
                OnlinePluginState.OutDated -> Button(shape = CircleShape, colors = buttonColors(
                    contentColor = MaterialTheme.colors.onBackground,
                    backgroundColor = MaterialColors.Green700
                ) ,onClick = { clickPluginAction.update(plugin) }) {
                    Text(text = stringResource(R.string.update_plugin))
                }
                OnlinePluginState.NotInstalled -> Button(shape = CircleShape, colors = buttonColors(
                    contentColor = MaterialTheme.colors.onBackground,
                    backgroundColor = MaterialTheme.colors.background
                ), onClick = { clickPluginAction.install(plugin) }) {
                    Text(text = stringResource(id = R.string.install_plugin))
                }
            }
        }
        if (expand) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                MarkdownText(markdown = plugin.markdown, Modifier.padding(horizontal = 8.dp))
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

private val JsBean.markdown
    get() = """
        By **${this.author}**    **v${this.version}**  
        ${this.description.replace("[Markdown]", "")}  
    """.trimPreSpace.also {
        Log.d(TAG, "显示的东西:\n $it")
    }

private val String.trimPreSpace
    get() = this.split("\n").joinToString("\n") { it.trimStart() }.trimStart()