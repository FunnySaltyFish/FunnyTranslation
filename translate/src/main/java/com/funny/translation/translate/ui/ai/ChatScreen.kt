package com.funny.translation.translate.ui.ai

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.funny.translation.helper.SimpleAction
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.LocalNavController
import com.funny.translation.translate.R
import com.funny.translation.translate.ui.ai.componets.ChatInputTextField
import com.funny.translation.translate.ui.ai.componets.MessageItem
import com.funny.translation.translate.ui.long_text.Category
import com.funny.translation.translate.ui.long_text.ModelListPart
import com.funny.translation.translate.ui.long_text.components.AIPointText
import com.funny.translation.translate.ui.widget.CommonPage
import com.funny.translation.ui.FixedSizeIcon
import kotlinx.coroutines.launch

// Modified From https://github.com/prafullmishra/JetComposer/tree/master

@Composable
fun ChatScreen() {
    val vm: ChatViewModel = viewModel()
    val inputText by vm.inputText
    val chatBot by vm.chatBot
    val chatMessages = vm.messages
    val navController = LocalNavController.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    BackHandler(drawerState.currentValue == DrawerValue.Open) {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        modifier = Modifier.fillMaxSize(),
        drawerState = drawerState,
        content = {
            ChatContent(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                chatBot = chatBot,
                currentMessageProvider = { vm.currentMessage },
                chatMessages = chatMessages,
                inputText = inputText,
                onInputTextChanged = vm::updateInputText,
                expandDrawerAction = { scope.launch { drawerState.open() } },
                sendAction = { vm.ask(inputText) },
                clearAction = vm::clearMessages,
                removeMessageAction = vm::removeMessage,
                doRefreshAction = vm::doRefresh
            )
        },
        drawerContent = {
            Settings(
                modifier = Modifier
                    .width(360.dp)
                    .fillMaxHeight()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                    )
                    .statusBarsPadding()
                    .padding(12.dp),
                vm = vm
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
    expandDrawerAction: () -> Unit,
    sendAction: () -> Unit,
    clearAction: () -> Unit,
    removeMessageAction: (ChatMessage) -> Unit,
    doRefreshAction: SimpleAction
) {
    CommonPage(
        modifier = modifier,
        title = chatBot.name,
        actions = {
            AIPointText()
        },
        navigationIcon = {
            IconButton(onClick = expandDrawerAction) {
                FixedSizeIcon(Icons.Filled.Menu, contentDescription = "Menu")
            }
        }
    ) {
        val lazyListState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        ChatMessageList(
            modifier = Modifier.weight(1f),
            currentMessageProvider = currentMessageProvider,
            chats = chatMessages,
            lazyListState = lazyListState,
            removeMessageAction = removeMessageAction,
            doRefreshAction = doRefreshAction
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
            },
            clearAction = clearAction
        )
    }
}

@Composable
@Preview
fun ChatBottomBar(
    text: String = "",
    onTextChanged: (String) -> Unit = {},
    sendAction: () -> Unit = {},
    clearAction: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
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
            sendAction = sendAction,
            clearAction = clearAction
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatMessageList(
    modifier: Modifier,
    lazyListState: LazyListState,
    currentMessageProvider: () -> ChatMessage?,
    chats: List<ChatMessage>,
    removeMessageAction: (ChatMessage) -> Unit,
    doRefreshAction: SimpleAction
) {
    val currentMessage = currentMessageProvider()
    val context = LocalContext.current
    val msgItem: @Composable LazyItemScope.(msg: ChatMessage, refreshAction: SimpleAction?) -> Unit = @Composable {  msg, refreshAction ->
        MessageItem(
            modifier = Modifier.animateItemPlacement(),
            chatMessage = msg,
            copyAction = {
                ClipBoardUtil.copy(context, msg.content)
                context.toastOnUi(R.string.copied_to_clipboard)
            },
            deleteAction = {
                removeMessageAction(msg)
            },
            refreshAction = refreshAction
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
        modifier = modifier,
        state = lazyListState
    ) {
        itemsIndexed(chats, key = { _, msg -> msg.id }, contentType = { _ , msg -> msg.type }) { i, message ->
            msgItem(message, if (!message.sendByMe && i == chats.lastIndex) doRefreshAction else null)
        }
        if (currentMessage != null) {
            item {
                msgItem(currentMessage, doRefreshAction)
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
        Category(title = "Prompt", helpText = stringResource(id = R.string.chat_prompt_help)) {
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

        ModelListPart(modelList = vm.modelList, vm.selectedModelId, onModelSelected = vm::updateBot)
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