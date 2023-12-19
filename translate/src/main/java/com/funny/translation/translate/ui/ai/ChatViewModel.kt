package com.funny.translation.translate.ui.ai

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewModelScope
import com.funny.compose.ai.bean.ChatMemoryFixedMsgLength
import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.bean.ChatMessageReq
import com.funny.compose.ai.bean.ChatMessageTypes
import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.bean.SENDER_ME
import com.funny.compose.ai.bean.StreamMessage
import com.funny.compose.ai.chat.ChatBot
import com.funny.compose.ai.chat.ModelChatBot
import com.funny.compose.ai.service.AskStreamRequest
import com.funny.compose.ai.service.aiService
import com.funny.compose.ai.service.askAndProcess
import com.funny.compose.ai.utils.ModelManager
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.codeeditor.base.BaseViewModel
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.NAV_ANIM_DURATION
import com.funny.translation.helper.displayMsg
import com.funny.translation.helper.string
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.R
import com.funny.translation.translate.appCtx
import com.funny.translation.translate.database.appDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatViewModel: BaseViewModel(appCtx) {
    private val dao = appDB.chatHistoryDao

    val inputText = mutableStateOf("")
    val chatBot: MutableState<ChatBot> = mutableStateOf(ModelChatBot.Empty)
    val messages: SnapshotStateList<ChatMessage> = mutableStateListOf()
    var currentMessage: ChatMessage? by mutableStateOf(null)
    val convId: MutableState<String?> = mutableStateOf(null)
    var systemPrompt by mutableDataSaverStateOf(DataSaverUtils, "key_chat_base_prompt", string(R.string.chat_system_prompt))
    val memory = ChatMemoryFixedMsgLength(3)

    var checkingPrompt by mutableStateOf(false)

    var modelList = mutableStateListOf<Model>()
    var selectedModelId by mutableDataSaverStateOf(DataSaverUtils,"selected_chat_model_id", 0)

    private var job: Job? = null

    init {
        // TODO 更改为多个 ConvId 的支持
        convId.value = "convId"

        execute {
            delay(NAV_ANIM_DURATION.toLong())
            modelList.addAll(ModelManager.models.await())

            if (modelList.isEmpty()) return@execute

            chatBot.value = (modelList.find { it.chatBotId == selectedModelId } ?: modelList[0]).let {
                ModelChatBot(it)
            }
        }

        execute {
            messages.addAll(dao.getMessagesByConversationId(convId.value!!))
        }
    }

    private fun addMessage(chatMessage: ChatMessage) {
        messages.add(chatMessage)
        execute {
            dao.insert(chatMessage)
        }
    }

    private fun addMessage(sender: String, message: String) {
        val convId = convId.value ?: return
        addMessage(
            ChatMessage(
                botId = chatBot.value.id,
                conversationId = convId,
                sender = sender,
                content = message,
                type = ChatMessageTypes.TEXT
            )
        )
    }

    private fun addErrorMessage(error: String) {
        val convId = convId.value ?: return
        addMessage(
            ChatMessage(
                botId = chatBot.value.id,
                conversationId = convId,
                sender = chatBot.value.name,
                content = "",
                error = error,
                type = ChatMessageTypes.ERROR
            )
        )
    }

    fun ask(message: String){
        if (message.isEmpty()) return
        convId.value ?: return
        addMessage(SENDER_ME, message)
        inputText.value = ""
        startAsk(message)
    }

    private fun startAsk(message: String) {
        job = viewModelScope.launch(Dispatchers.IO) {
            chatBot.value.chat(convId.value, message, messages, systemPrompt, memory).collect {
                when (it) {
                    is StreamMessage.Start -> {
                        currentMessage = ChatMessage(
                            botId = chatBot.value.id,
                            conversationId = convId.value!!,
                            sender = chatBot.value.name,
                            content = "",
                            type = it.type
                        )
                    }
                    is StreamMessage.Part -> {
                        val msg = currentMessage
                        currentMessage = msg?.copy(content = msg.content + it.part)
                    }
                    is StreamMessage.End -> {
                        addMessage(currentMessage!!)
                        currentMessage = null
                    }
                    is StreamMessage.Error -> {
                        addErrorMessage(it.error.removePrefix("<<error>>"))
                        currentMessage = null
                    }
                }
            }
        }
    }

    fun checkPrompt(newPrompt: String) {
        if (checkingPrompt) return
        checkingPrompt = true
        execute {
            try {
                val txt = aiService.askAndProcess(
                    AskStreamRequest(
                        chatBot.value.id,
                        listOf(ChatMessageReq("user", newPrompt)),
                        CHECK_PROMPT_PROMPT,
                    )
                ).lowercase()

                when (txt) {
                    "true" -> systemPrompt = newPrompt
                    "false" -> appCtx.toastOnUi(R.string.not_correct_prompt)
                    else -> appCtx.toastOnUi(string(R.string.unparseable_prompt, txt))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                appCtx.toastOnUi(e.displayMsg())
            }
            checkingPrompt = false
        }
    }

    fun clearMessages() {
        messages.clear()
        execute {
            dao.clearMessagesByConversationId(convId.value!!)
        }
    }

    fun removeMessage(message: ChatMessage) {
        messages.remove(message)
        execute {
            dao.delete(message)
        }
    }

    fun doRefresh() {
        job?.cancel()
        if (currentMessage == null) {
            removeMessage(messages.last())
        } else {
            currentMessage = null
        }
        val lastMyMsg = messages.last()
        startAsk(lastMyMsg.content)
    }
    
    fun updateInputText(text: String) { inputText.value = text }
    fun updateSystemPrompt(prompt: String) { systemPrompt = prompt }
    fun updateBot(model: Model) {
        chatBot.value = ModelChatBot(model)
        selectedModelId = model.chatBotId
    }




    companion object {
        private const val BASE_PROMPT = "You're ChatGPT, a helpful AI assistant."
        private const val CHECK_PROMPT_PROMPT = "请判定下面的Prompt是否和外语类学习、翻译类主题相关，你应该只返回一个布尔值，true或者false。必须精准的返回："
    }
}