package com.funny.translation.translate.ui.ai

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.compose.ai.bean.ChatMemoryFixedMsgLength
import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.bean.ChatMessageTypes
import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.bean.SENDER_ME
import com.funny.compose.ai.bean.StreamMessage
import com.funny.compose.ai.chat.ChatBot
import com.funny.compose.ai.chat.ModelChatBot
import com.funny.compose.ai.chat.TestChatBot
import com.funny.compose.ai.service.aiService
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.helper.DataSaverUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel: ViewModel() {
    val inputText = mutableStateOf("")
    val chatBot: MutableState<ChatBot> = mutableStateOf(TestChatBot())
    val messages: MutableState<List<ChatMessage>> = mutableStateOf(listOf())
    val currentMessage: MutableState<ChatMessage?> = mutableStateOf(null)
    val convId: MutableState<String?> = mutableStateOf(null)
    val systemPrompt = mutableDataSaverStateOf(DataSaverUtils, "key_chat_base_prompt", BASE_PROMPT)
    val memory = mutableDataSaverStateOf(DataSaverUtils, "key_chat_memory", ChatMemoryFixedMsgLength(2))
    val models = mutableStateOf(listOf<Model>())
    init {
        chatBot.value = TestChatBot()
        convId.value = "convId"

//        addMessage(SENDER_ME, "Hello, I'm ${chatBot.value.name}.")
//        addMessage(chatBot.value.name, "I'm a test chat bot.\nSo\nIt is important to see something interesting.")

        viewModelScope.launch {
            val modelList = kotlin.runCatching {
                aiService.getChatModels()
            }.onFailure { it.printStackTrace() }.getOrDefault(listOf())

            if (modelList.isEmpty()) return@launch

            models.value = modelList
            chatBot.value = ModelChatBot(modelList[0])
        }
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

    private fun addErrorMessage(error: String) {
        val convId = convId.value ?: return
        addMessage(
            ChatMessage(
                UUID.randomUUID().toString(),
                chatBot.value.id,
                convId,
                chatBot.value.name,
                error,
                ChatMessageTypes.ERROR
            )
        )
    }

    fun ask(message: String){
        if (message.isEmpty()) return
        convId.value ?: return
        addMessage(SENDER_ME, message)
        inputText.value = ""

        viewModelScope.launch(Dispatchers.IO) {
            chatBot.value.chat(convId.value, message, messages.value, systemPrompt.value, memory.value).collect {
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
                    is StreamMessage.Error -> {
                        addErrorMessage(it.error)
                        currentMessage.value = null
                    }
                }
            }
        }
    }
    
    fun updateInputText(text: String) { inputText.value = text }

    companion object {
        private const val BASE_PROMPT = "You're ChatGPT, a helpful AI assistant."
    }
}