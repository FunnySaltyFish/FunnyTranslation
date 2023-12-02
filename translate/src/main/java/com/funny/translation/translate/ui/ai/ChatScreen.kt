package com.funny.translation.translate.ui.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.chat.ChatBot
import com.funny.translation.debug.rememberStateOf
import com.funny.translation.helper.ClipBoardUtil
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.LocalNavController
import com.funny.translation.translate.R
import com.funny.translation.translate.ui.ai.componets.ChatInputTextField
import com.funny.translation.translate.ui.ai.componets.MessageItem
import com.funny.translation.translate.ui.long_text.Category
import com.funny.translation.translate.ui.long_text.ModelListPart
import com.funny.translation.translate.ui.long_text.components.AIPointText
import com.funny.translation.translate.ui.widget.CommonPage
import kotlinx.coroutines.launch

// Modified From https://github.com/prafullmishra/JetComposer/tree/master

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatScreen() {
    val vm: ChatViewModel = viewModel()
    val inputText by vm.inputText
    val chatBot by vm.chatBot
    val chatMessages by vm.messages
    val navController = LocalNavController.current

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
                onUpPressed = {
                    navController.navigateUp()
                },
                sendAction = { vm.ask(inputText) }
            )
        },
        drawerContent = {
            Settings(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .fillMaxHeight()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                    )
                    .statusBarsPadding()
                    .padding(12.dp)
                , vm = vm
            )
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
    CommonPage(
        modifier = modifier,
        title = chatBot.name,
        actions = {
            AIPointText()
        }
    ) {
        val lazyListState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        ChatMessageList(
            modifier = Modifier.weight(1f),
            currentMessageProvider = currentMessageProvider,
            chats = chatMessages,
            lazyListState = lazyListState
        )
        ChatBottomBar(
            text = inputText,
            onTextChanged = onInputTextChanged,
            sendAction = {
                if (chatMessages.size > 1) {
                    scope.launch {
                        lazyListState.animateScrollToItem(chatMessages.size - 1)
                    }
                }
                sendAction()
            }
        )
    }
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
    lazyListState: LazyListState,
    currentMessageProvider: () -> ChatMessage?,
    chats: List<ChatMessage>
) {
    val currentMessage = currentMessageProvider()
    val context = LocalContext.current
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
        modifier = modifier,
        state = lazyListState
    ) {
        items(chats) { message ->
            MessageItem(message, copyAction = {}, deleteAction = {}, refreshAction = {})
        }
        if (currentMessage != null) {
            item {
                MessageItem(
                    currentMessage,
                    copyAction = {
                        ClipBoardUtil.copy(context, currentMessage.content)
                        context.toastOnUi(R.string.copied_to_clipboard)
                    },
                    deleteAction = {},
                    refreshAction = {}
                )
            }
        }
    }
}

@Composable
private fun Settings(
    modifier: Modifier,
    vm: ChatViewModel,
) {
    Column(modifier) {
        // Prompt
        Category(title = "Prompt") {
            var text by rememberStateOf(value = vm.systemPrompt)
            TextField(value = text, onValueChange = { text = it })
            val showConfirmButton by remember {
                derivedStateOf { text != vm.systemPrompt }
            }

            AnimatedVisibility(visible = showConfirmButton) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { text = vm.systemPrompt },
                    ) {
                        Text(text = stringResource(id = R.string.reset))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    TaskButton(
                        onClick = { vm.checkPrompt(text) },
                        loading = vm.checkingPrompt
                    ) {
                        Text(text = stringResource(id = R.string.check_and_modify))
                    }
                }
            }
        }

        ModelListPart(onBotSelected = vm::updateBot)
    }
}

// 做某项任务的 button，点击后前面加上圈圈，并且不可点击，直至任务完成或者失败
@Composable
fun TaskButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    loading: Boolean = false,
    enabled: Boolean = true,
    loadingColor: Color = MaterialTheme.colorScheme.primary,
    loadingContent: @Composable () -> Unit = {
        CircularProgressIndicator(
            color = loadingColor,
            strokeWidth = 2.dp,
            modifier = Modifier.size(16.dp)
        )
    },
    content: @Composable () -> Unit
) {
    val loadingModifier = Modifier
        .clickable(enabled = enabled, onClick = onClick)
        .animateContentSize()
        .then(modifier)

    Button(
        onClick = onClick,
        enabled = enabled and !loading,
        modifier = loadingModifier
    ) {
        AnimatedVisibility(visible = loading) {
            loadingContent()
        }
        content()
    }
}