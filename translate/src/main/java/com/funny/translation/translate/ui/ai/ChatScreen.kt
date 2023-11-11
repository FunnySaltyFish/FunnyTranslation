package com.funny.translation.translate.ui.ai

import android.animation.ArgbEvaluator
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.chat.ChatBot
import com.funny.compose.ai.utils.getColorAtProgress
import com.funny.translation.translate.R
import com.funny.translation.translate.ui.ai.componets.ChatInputTextField
import com.funny.translation.translate.ui.ai.componets.MessageItem
import com.funny.translation.ui.FixedSizeIcon

// Modified From https://github.com/prafullmishra/JetComposer/tree/master

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(onUpPressed: () -> Unit) {
    val vm: ChatViewModel = viewModel()
    val inputText by vm.inputText
    val chatBot by vm.chatBot
    val chatMessages by vm.messages

    ModalNavigationDrawer(
        modifier = Modifier.fillMaxSize(),
        content = {
            ChatContent(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                chatBot = chatBot,
                currentMessageProvider = { vm.currentMessage.value },
                chatMessages = chatMessages,
                inputText = inputText,
                onInputTextChanged = vm::updateInputText,
                onUpPressed = onUpPressed,
                sendAction = { vm.ask(inputText) }
            )
        },
        drawerContent = {

        }
    )
}

@Composable
fun ChatContent(
    modifier: Modifier,
    chatBot: ChatBot,
    currentMessageProvider: () -> ChatMessage?,
    chatMessages: List<ChatMessage>,
    inputText: String,
    onInputTextChanged: (String) -> Unit,
    sendAction: () -> Unit,
    onUpPressed: () -> Unit
) {
    Column(modifier) {
        ChatTopBar(onUpPressed, chatBot.name)
        ChatMessageList(
            modifier = Modifier.weight(1f),
            currentMessageProvider = currentMessageProvider,
            chats = chatMessages
        )
        ChatBottomBar(
            text = inputText,
            onTextChanged = onInputTextChanged,
            sendAction = sendAction
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(onUpPressed: () -> Unit, botName: String) {
    TopAppBar(
        title = {
            Text(text = botName)
        }
    )
}

@Composable
@Preview
fun ChatBottomBar(
    text: String = "",
    onTextChanged: (String) -> Unit = {},
    sendAction: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(50)
            )
            .padding(6.dp)
    ) {
        ChatInputTextField(
            modifier = Modifier.weight(1f),
            input = text,
            onValueChange = onTextChanged,
            sendAction = sendAction
        )
    }
}

@Composable
fun ChatMessageList(
    modifier: Modifier,
    currentMessageProvider: () -> ChatMessage?,
    chats: List<ChatMessage>
) {
    val currentMessage = currentMessageProvider()
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
        modifier = modifier
    ) {
        items(chats) { message ->
            MessageItem(message, copyAction = {}, deleteAction = {}, refreshAction = {})
        }
        if (currentMessage != null) {
            item {
                MessageItem(
                    currentMessage,
                    copyAction = {},
                    deleteAction = {},
                    refreshAction = {}
                )
            }
        }
    }

    Recomposer
}

@Composable
fun SentMessage(
    chat: String,
    isPrevSent: Boolean,
    isNextSent: Boolean,
    isEmojiOnly: Boolean,
    isNextEmojiOnly: Boolean,
    isPrevEmojiOnly: Boolean,
    listHeight: Float
) {
    val evaluator = remember { ArgbEvaluator() }
    val topShade = remember { Color(0xFFB500E7) }
    val bottomShade = remember { Color(0xFF1261FF) }
    var backgroundColor by remember { mutableStateOf(bottomShade) }

    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, bottom = if (!isNextSent) 0.dp else 24.dp)
            .onGloballyPositioned { coordinates: LayoutCoordinates ->
                if (listHeight > 0f) {
                    val topOffset = coordinates.boundsInParent().top
                    val cleanTopOffset = when {
                        topOffset < 0 -> 0f
                        topOffset > listHeight -> listHeight
                        else -> topOffset
                    }
                    backgroundColor = getColorAtProgress(
                        progress = cleanTopOffset / listHeight,
                        start = topShade,
                        end = bottomShade,
                        evaluator = evaluator
                    )
                }
            }
    ) {
        var fontSize = 15.sp
        var textModifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    bottomStart = 18.dp,
                    topEnd = if (isPrevSent || isPrevEmojiOnly) 18.dp else 3.dp,
                    bottomEnd = if (isNextSent || isNextEmojiOnly) 18.dp else 3.dp
                )
            )
            .padding(vertical = 8.dp, horizontal = 12.dp)

        if (isEmojiOnly) {
            fontSize = 36.sp
            textModifier = Modifier
                .padding(vertical = 8.dp, horizontal = 12.dp)
        }

        Spacer(Modifier.weight(0.2f))
        Box(
            contentAlignment = Alignment.CenterEnd,
            modifier = Modifier.weight(0.8f)
        ) {
            Text(
                text = chat,
                color = Color.White,
                modifier = textModifier,
                fontSize = fontSize
            )
        }
    }
}

@Composable
fun ReceivedMessage(
    chat: String,
    isPrevReceived: Boolean,
    isNextReceived: Boolean,
    isEmojiOnly: Boolean,
    isNextEmojiOnly: Boolean,
    isPrevEmojiOnly: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, bottom = if (!isNextReceived) 0.dp else 24.dp)
    ) {
        if (isNextReceived) {
            Spacer(Modifier.width(10.dp))
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
            ) {
                FixedSizeIcon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_round_android_24),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
        } else {
            Spacer(Modifier.width(48.dp))
        }
        var fontSize = 15.sp
        var textModifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(
                    topEnd = 18.dp,
                    bottomEnd = 18.dp,
                    topStart = if (isPrevReceived || isPrevEmojiOnly) 18.dp else 3.dp,
                    bottomStart = if (isNextReceived || isNextEmojiOnly) 18.dp else 3.dp
                )
            )
            .padding(vertical = 8.dp, horizontal = 12.dp)
        if (isEmojiOnly) {
            fontSize = 36.sp
            textModifier = Modifier
                .padding(vertical = 8.dp, horizontal = 12.dp)
        }
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier.weight(0.9f)
        ) {
            Text(
                text = chat,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = textModifier,
                fontSize = fontSize
            )
        }
        Spacer(Modifier.weight(0.1f))
    }
}