package com.funny.translation.translate.ui.ai

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.compose.ai.bean.ChatMemoryFixedMsgLength
import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.bean.ChatMessageReq
import com.funny.compose.ai.bean.ChatMessageTypes
import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.bean.SENDER_ME
import com.funny.compose.ai.bean.StreamMessage
import com.funny.compose.ai.chat.ChatBot
import com.funny.compose.ai.chat.ChatBots
import com.funny.compose.ai.chat.ModelChatBot
import com.funny.compose.ai.service.AskStreamRequest
import com.funny.compose.ai.service.aiService
import com.funny.compose.ai.service.askAndProcess
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.displayMsg
import com.funny.translation.helper.string
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.R
import com.funny.translation.translate.appCtx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel: ViewModel() {
    val inputText = mutableStateOf("")
    val chatBot: MutableState<ChatBot> = mutableStateOf(ModelChatBot.Empty)
    val messages: MutableState<List<ChatMessage>> = mutableStateOf(listOf())
    val currentMessage: MutableState<ChatMessage?> = mutableStateOf(null)
    val convId: MutableState<String?> = mutableStateOf(null)
    var systemPrompt by mutableDataSaverStateOf(DataSaverUtils, "key_chat_base_prompt", string(R.string.chat_system_prompt))
    val memory = mutableDataSaverStateOf(DataSaverUtils, "key_chat_memory", ChatMemoryFixedMsgLength(2))

    var modelList = mutableStateListOf<Model>()

    var checkingPrompt by mutableStateOf(false)

    init {
        convId.value = "convId"

//        addMessage(SENDER_ME, "Hello, I'm ${chatBot.value.name}.")
//        addMessage(chatBot.value.name, "I'm a test chat bot.\nSo\nIt is important to see something interesting.")

        viewModelScope.launch {
            modelList = kotlin.runCatching {
                aiService.getChatModels()
            }.onFailure { it.printStackTrace() }.getOrDefault(listOf()).toMutableStateList()

            if (modelList.isEmpty()) return@launch

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
            chatBot.value.chat(convId.value, message, messages.value, systemPrompt, memory.value).collect {
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

    fun checkPrompt(newPrompt: String) {
        if (checkingPrompt) return
        checkingPrompt = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val txt = aiService.askAndProcess(
                    AskStreamRequest(
                        chatBot.value.id,
                        listOf(ChatMessageReq("user", newPrompt)),
                        CHECK_PROMPT_PROMPT,
                    )
                ).lowercase()

                if (txt == "true") {
                    systemPrompt = newPrompt
                } else if (txt == "false") {
                    appCtx.toastOnUi(R.string.not_correct_prompt)
                } else {
                    appCtx.toastOnUi(string(R.string.unparseable_prompt, txt))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                appCtx.toastOnUi(e.displayMsg())
            }
            checkingPrompt = false
        }
    }
    
    fun updateInputText(text: String) { inputText.value = text }
    fun updateSystemPrompt(prompt: String) { systemPrompt = prompt }
    fun updateBot(id: Int) {
        ChatBots.findById(id)?.let { this.chatBot.value = it }
    }

    companion object {
        private const val BASE_PROMPT = "You're ChatGPT, a helpful AI assistant."
        private const val CHECK_PROMPT_PROMPT = "请判定下面的Prompt是否和外语类学习、翻译类主题相关，你应该只返回一个布尔值，true或者false。必须精准的返回："
    }
}