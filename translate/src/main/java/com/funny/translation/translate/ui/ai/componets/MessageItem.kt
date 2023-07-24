package com.funny.translation.translate.ui.ai.componets

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.bean.ChatMessageTypes
import com.funny.translation.helper.SimpleAction
import com.funny.translation.translate.R

@Composable
fun MessageItem(
    chatMessage: ChatMessage,
    copyAction: SimpleAction,
    deleteAction: SimpleAction
) {
    val sendByMe = chatMessage.sendByMe

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        horizontalAlignment = if (sendByMe) Alignment.End else Alignment.Start
    ) {
        Text(text = chatMessage.sender, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = if (sendByMe) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.Bottom
            ) {
                if (sendByMe) MessageItemMenuIcon(onCopy = copyAction, onDelete = deleteAction)
                when (chatMessage.type) {
                    ChatMessageTypes.TEXT ->
                        Text(
                            text = chatMessage.content,
                            modifier = Modifier
                                .widthIn(0.dp, 300.dp)
                                .background(
                                    if (sendByMe) MaterialTheme.colorScheme.primaryContainer else Color(
                                        247,
                                        249,
                                        253
                                    ),
                                    RoundedCornerShape(
                                        topStart = if (sendByMe) 8.dp else 0.dp,
                                        topEnd = if (sendByMe) 0.dp else 8.dp,
                                        bottomEnd = 8.dp,
                                        bottomStart = 8.dp
                                    )
                                )
                                .padding(8.dp),
                            color = if (sendByMe) Color.White else Color.Black
                        )

                    ChatMessageTypes.IMAGE ->
                        Image(
                            painter = rememberAsyncImagePainter(model = chatMessage.content),
                            contentDescription = "generated image"
                        )
                }
                if (!sendByMe) MessageItemMenuIcon(onCopy = copyAction, onDelete = deleteAction)
            }

    }
}

@Composable
private fun MessageItemMenuIcon(
    modifier: Modifier = Modifier,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    var expandMenu by remember { mutableStateOf(false) }
    IconButton(onClick = { expandMenu = true }) {
        Icon(Icons.Default.Menu, contentDescription = stringResource(id = R.string.menu))
        DropdownMenu(expanded = expandMenu, onDismissRequest = { expandMenu = false }) {
            // 复制、删除
            DropdownMenuItem(
                text = {
                    Text(text = stringResource(id = R.string.copy_content))
                },
                onClick = {
                    onCopy()
                    expandMenu = false
                }
            )
            DropdownMenuItem(
                text = {
                    Text(text = stringResource(id = R.string.delete_message))
                },
                onClick = {
                    onDelete()
                    expandMenu = false
                }
            )


        }
    }
}