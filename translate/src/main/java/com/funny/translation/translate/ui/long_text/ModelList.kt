package com.funny.translation.translate.ui.long_text

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.chat.ChatBots
import com.funny.compose.ai.chat.ServerChatBot
import com.funny.compose.loading.loadingList
import com.funny.compose.loading.rememberRetryableLoadingState
import com.funny.translation.debug.rememberStateOf
import com.funny.translation.helper.string
import com.funny.translation.translate.R
import com.funny.translation.translate.network.TransNetwork
import kotlin.math.roundToInt

private val aiService = TransNetwork.aiService

@Composable
fun ColumnScope.ModelListPart(
    onBotSelected: (botId: Int) -> Unit
) {
    Category(
        title = stringResource(id = R.string.model_select),
        helpText = stringResource(id = R.string.model_select_help)
    ) { expanded ->
        val (state, retry) = rememberRetryableLoadingState(loader = aiService::getChatModels)
        var currentSelectBotId by rememberStateOf(value = 0)
        val height by animateDpAsState(targetValue = if (expanded) 400.dp else 200.dp, label = "height")
        val onClick = { chatBotId: Int ->
            currentSelectBotId = chatBotId
            onBotSelected(chatBotId)
        }
        LazyColumn(modifier = Modifier
            .fillMaxWidth()
            .heightIn(0.dp, height)) {
            loadingList(state, retry = retry, key = { it.chatBotId }) {
                val chatBot = ChatBots.findById(it.chatBotId) ?: return@loadingList
                if (currentSelectBotId == 0) {
                    onClick(it.chatBotId)
                }
                ListItem(
                    modifier = Modifier.clickable { onClick(it.chatBotId) },
                    headlineContent = {
                        Text(text = chatBot.name)
                    },
                    supportingContent = {
                        Text(text = chatBot.description(it), fontSize = 12.sp)
                    },
                    trailingContent = {
                        RadioButton(selected = currentSelectBotId == it.chatBotId, onClick = {
                            onClick(it.chatBotId)
                        })
                    },
                )
            }
        }
    }
}

fun ServerChatBot.description(model: Model): String
    = "${string(R.string.context_length)} ${ ((model.maxContextTokens)/1000f).roundToInt() }k | ${string(R.string.currency_symbol)}${model.cost1kChars} / ${string(R.string.kilo_char)} | ${model.description}"