package com.funny.translation.translate.ui.plugin

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.WebViewActivity
import com.funny.translation.helper.SimpleAction
import com.funny.translation.js.bean.JsBean
import com.funny.translation.translate.R
import com.funny.translation.ui.MarkdownText
import com.funny.translation.ui.touchToScale


private const val TAG = "OnlinePlugin"

enum class OnlinePluginState {
    Installed, OutDated, NotInstalled
}

internal interface ClickOnlinePluginAction {
    fun install(jsBean: JsBean)
    fun delete(jsBean: JsBean)
    fun update(jsBean: JsBean)
}

@Composable
internal fun OnlinePluginItem(
    plugin: JsBean,
    onlinePluginState: OnlinePluginState,
    clickOnlinePluginAction: ClickOnlinePluginAction
) {
    var expand by remember {
        mutableStateOf(false)
    }
    val flatButton = @Composable { title: String, color: Color, onClick: SimpleAction ->
        Button(
            shape = CircleShape,
            colors = buttonColors(
                contentColor = Color.White,
                containerColor = color
            ),
            contentPadding = PaddingValues(horizontal = 40.dp, vertical = 0.dp),
            onClick = onClick
        ) {
            Text(title)
        }
    }
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
                .padding(horizontal = 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(plugin.fileName, fontSize = 22.sp, fontWeight = FontWeight.W600)
            when (onlinePluginState) {
                OnlinePluginState.Installed -> flatButton(
                    stringResource(id = R.string.uninstall_plugin),
                    Color(244, 67, 54)
                ) {
                    clickOnlinePluginAction.delete(plugin)
                }
                OnlinePluginState.OutDated -> flatButton(
                    stringResource(id = R.string.update_plugin),
                    Color(76, 175, 80)
                ) {
                    clickOnlinePluginAction.update(plugin)
                }
                OnlinePluginState.NotInstalled -> flatButton(
                    stringResource(id = R.string.install_plugin),
                    MaterialTheme.colorScheme.primary
                ) {
                    clickOnlinePluginAction.install(plugin)
                }
            }
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
            Text(
                modifier = Modifier.padding(bottom = 4.dp),
                text = stringResource(R.string.plugin_info_template, plugin.author, plugin.version),
                fontWeight = FontWeight.W600
            )
        }
    }
}

internal val JsBean.markdown
    get() = this.description.replace("[Markdown]", "")