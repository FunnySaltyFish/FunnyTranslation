package com.funny.translation.translate.ui.ai

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.compose.ai.ChatBot
import com.funny.compose.ai.TestChatBot
import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.bean.ChatMessageTypes
import com.funny.compose.ai.bean.SENDER_ME
import com.funny.compose.ai.bean.StreamMessage
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel: ViewModel() {
    val inputText = mutableStateOf("")
    val chatBot: MutableState<ChatBot> = mutableStateOf(TestChatBot())
    val messages: MutableState<List<ChatMessage>> = mutableStateOf(listOf())
    val currentMessage: MutableState<ChatMessage?> = mutableStateOf(null)
    val convId: MutableState<String?> = mutableStateOf(null)
    
    init {
        chatBot.value = TestChatBot()
        convId.value = "convId"
    }

    private fun addMessage(chatMessage: ChatMessage) {
        messages.value = messages.value + chatMessage
    }

    private fun addMessage(sender: String, message: String) {
        val convId = convId.value ?: return
        addMessage(
            ChatMessage(
                UUID.randomUUID().toString(),
                chatBot.value.id,
                convId,
                sender,
                message,
                ChatMessageTypes.TEXT
            )
        )
    }

    fun ask(message: String){
        if (message.isEmpty()) return
        convId.value ?: return
        addMessage(SENDER_ME, message)
        inputText.value = ""

        viewModelScope.launch {
            chatBot.value.chat(convId.value, message).collect {
                when (it) {
                    is StreamMessage.Start -> {
                        currentMessage.value = ChatMessage(
                            it.id,
                            chatBot.value.id,
                            convId.value!!,
                            chatBot.value.name,
                            "",
                            it.type
                        )
                    }
                    is StreamMessage.Part -> {
                        val msg = currentMessage.value
                        currentMessage.value = msg?.copy(content = msg.content + it.part)
                    }
                    is StreamMessage.End -> {
                        addMessage(currentMessage.value!!)
                        currentMessage.value = null
                    }
                }
            }
        }
    }
    
    fun updateInputText(text: String) { inputText.value = text }
}