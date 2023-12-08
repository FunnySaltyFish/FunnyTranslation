package com.funny.translation.translate.ui.ai.componets

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.bean.ChatMessageTypes
import com.funny.jetsetting.core.ui.FunnyIcon
import com.funny.jetsetting.core.ui.IconWidget
import com.funny.translation.helper.SimpleAction
import com.funny.translation.ui.MarkdownText

@Composable
fun MessageItem(
    modifier: Modifier = Modifier,
    chatMessage: ChatMessage,
    copyAction: SimpleAction,
    deleteAction: SimpleAction,
    refreshAction: SimpleAction
) {
    val sendByMe = chatMessage.sendByMe

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = if (sendByMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .requiredWidthIn(0.dp, 300.dp),
            horizontalAlignment = if (sendByMe) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .background(
                        if (sendByMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer, //Color(247,249,253),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(10.dp)
                    .animateContentSize()
            ) {
                when (chatMessage.type) {
                    ChatMessageTypes.TEXT ->
                        if (sendByMe) {
                            Text(
                                text = chatMessage.content,
                                modifier = Modifier,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            val content = if (chatMessage.error != null) {
                                chatMessage.content + "\n" + chatMessage.error
                            } else {
                                chatMessage.content.ifEmpty { "..." }
                            }
                            MarkdownText(
                                markdown = content,
                                color = if (chatMessage.error != null)
                                    MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                    ChatMessageTypes.IMAGE ->
                        Image(
                            painter = rememberAsyncImagePainter(model = chatMessage.content),
                            contentDescription = "generated image"
                        )

                    ChatMessageTypes.ERROR ->
                        Text(
                            text = chatMessage.error ?: "Unknown Error",
                            modifier = Modifier,
                            color = MaterialTheme.colorScheme.error
                        )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                Modifier
                    .background(
                        MaterialTheme.colorScheme.tertiaryContainer,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                // refresh / copy / delete
                if (chatMessage.type == ChatMessageTypes.TEXT) {
                    MessageItemMenuIcon(
                        icon = FunnyIcon(imageVector = Icons.Default.ContentCopy),
                        onClick = copyAction
                    )
                }
                MessageItemMenuIcon(
                    icon = FunnyIcon(imageVector = Icons.Default.Refresh),
                    onClick = refreshAction
                )
                MessageItemMenuIcon(
                    icon = FunnyIcon(imageVector = Icons.Default.Delete),
                    onClick = deleteAction
                )
            }
        }
    }
}

@Composable
private fun MessageItemMenuIcon(
    modifier: Modifier = Modifier,
    icon: FunnyIcon,
    onClick: SimpleAction
) {
    IconWidget(funnyIcon = icon, modifier = modifier.clickable(onClick = onClick).padding(4.dp).size(20.dp))
}