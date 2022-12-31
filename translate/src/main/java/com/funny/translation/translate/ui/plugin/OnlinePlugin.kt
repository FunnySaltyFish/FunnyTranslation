package com.funny.translation.translate.ui.plugin

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.funny.translation.translate.extentions.trimLineStart
import com.funny.translation.translate.ui.widget.LoadingContent
import com.funny.translation.translate.ui.widget.MarkdownText
import com.funny.translation.ui.touchToScale


private const val TAG = "OnlinePlugin"

enum class OnlinePluginState {
    Installed, OutDated, NotInstalled
}

interface ClickOnlinePluginAction {
    fun install(jsBean: JsBean)
    fun delete(jsBean: JsBean)
    fun update(jsBean: JsBean)
}

@Composable
fun OnlinePluginItem(
    plugin: JsBean,
    onlinePluginState: OnlinePluginState,
    clickOnlinePluginAction: ClickOnlinePluginAction
) {
    var expand by remember {
        mutableStateOf(false)
    }
    Column(modifier = Modifier
        .touchToScale { expand = !expand }
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(MaterialTheme.colorScheme.primaryContainer)
        .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(plugin.fileName, fontSize = 18.sp, fontWeight = FontWeight.W600)
            when(onlinePluginState){
                OnlinePluginState.Installed -> Button(shape = CircleShape, colors = buttonColors(
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ) ,onClick = { clickOnlinePluginAction.delete(plugin) }) {
                    Text(stringResource(R.string.uninstall_plugin))
                }
                OnlinePluginState.OutDated -> Button(shape = CircleShape, colors = buttonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    containerColor = MaterialColors.Green200
                ) ,onClick = { clickOnlinePluginAction.update(plugin) }) {
                    Text(text = stringResource(R.string.update_plugin))
                }
                OnlinePluginState.NotInstalled -> Button(shape = CircleShape, colors = buttonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    containerColor = MaterialTheme.colorScheme.background
                ), onClick = { clickOnlinePluginAction.install(plugin) }) {
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
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

private val JsBean.markdown
    get() = """
        By **${this.author}**    **v${this.version}**  
        ${this.description.replace("[Markdown]", "")}  
    """.trimLineStart