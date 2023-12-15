package com.funny.translation.translate.ui.long_text

import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.funny.compose.ai.bean.ChatMemoryFixedMsgLength
import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.bean.ChatMessageReq
import com.funny.compose.ai.bean.Model
import com.funny.compose.ai.bean.SENDER_ME
import com.funny.compose.ai.bean.StreamMessage
import com.funny.compose.ai.chat.ModelChatBot
import com.funny.compose.ai.chat.TestLongTextChatBot
import com.funny.compose.ai.service.aiService
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.codeeditor.base.BaseViewModel
import com.funny.translation.helper.DataHolder
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.displayMsg
import com.funny.translation.helper.string
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.R
import com.funny.translation.translate.appCtx
import com.funny.translation.translate.database.LongTextTransTask
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.extentions.safeSubstring
import com.funny.translation.translate.ui.long_text.bean.TermList
import com.funnysaltyfish.partialjsonparser.JsonParseException
import com.funnysaltyfish.partialjsonparser.PartialJsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID


const val DEFAULT_PROMPT_PREFIX = """你现在是一名优秀的翻译人员，现在在翻译长文中的某个片段。请根据给定的术语表，将输入文本翻译成中文"""
const val DEFAULT_PROMPT_SUFFIX = """，并找出可能存在的新术语。你的输出分为两部分，以||sep||分割，前半部分是翻译结果，后半部分是关键词，以JSON数组的形式，无则为[]
示例输入：XiaoHong and XiaoMing are studying DB class.||sep||[["DB","数据库"],["XiaoHong","萧红"]]
示例输出：萧红和晓明正在学习数据库课程||sep||[["XiaoMing","晓明"]]"""

private val DEFAULT_PROMPT = EditablePrompt(DEFAULT_PROMPT_PREFIX, DEFAULT_PROMPT_SUFFIX)

private const val SEP = "||sep||"


@Serializable
private data class Answer(val text: String? = null, var keywords: List<List<String>>? = null)

internal enum class ScreenState {
    Init, Translating, Result
}

class LongTextTransViewModel: BaseViewModel(appCtx) {
    private val dao = appDB.longTextTransDao
    internal var task: LongTextTransTask? by mutableStateOf(null)
    internal var screenState by mutableStateOf(ScreenState.Init)

    var chatBot: ModelChatBot by mutableStateOf(TestLongTextChatBot())
    var modelList = mutableStateListOf<Model>()
    var selectedModelId by mutableDataSaverStateOf(DataSaverUtils,"selected_chat_model_id", 0)

    private var totalLength = 0
    var translatedLength by mutableIntStateOf(0)

    val progress by derivedStateOf {  if (translatedLength == 0) 0f else (translatedLength.toFloat() / totalLength).coerceIn(0f, 1f) }
    val startedProgress by derivedStateOf {  if (translatedLength == 0) 0f else ((translatedLength + currentTransPartLength).toFloat() / totalLength).coerceIn(0f, 1f) }

    private var translateJob: Job? = null
    private var dbJob: Job? = null
    // 是否正在编辑术语，是的话暂停一下翻译
    private var isEditingTerm: Boolean = false
    // 是否暂停
    var isPausing by mutableStateOf(false)

    var transId = UUID.randomUUID().toString()
    internal var prompt by mutableStateOf(DEFAULT_PROMPT)
    private var memory = ChatMemoryFixedMsgLength(2)

    val allCorpus = TermList()
    val currentCorpus = TermList()
    var sourceText by mutableStateOf("")
    var resultText by mutableStateOf("")

    var currentTransPartLength = 0 // 当前翻译的长度
    val currentResultStartOffset get() = lastResultText.length

    // 源文本翻译时的每一段结束位置，每一个值为该段的最后一个字符的索引
    val sourceTextSegments = mutableListOf<Int>()
    // 翻译结果的每一段结束位置，每一个值为该段的最后一个字符的索引
    val resultTextSegments = mutableListOf<Int>()

    // 当前 part 翻译得到的结果
    private var currentOutput = StringBuilder()
    // 已经完成的 parts 翻译得到的结果
    private var lastResultText = ""
    // 因各种问题导致的失败次数
    var errorTimes by mutableIntStateOf(0)

    private val record = true // 仅在调试时使用，记录所有的翻译输入与输出
    // 包括
    // {
    //      "sourceText": "XiaoHong and XiaoMing are studying DB class.",
    //      “process": [
    //          { “endIndex": 5, "output": ["{", "{\"text\", ... , ] },
    //          { “endIndex": 10, "output": ["{", "{\"text\", ... , ] },
    //       ]
    // }
    private val recordObj = JSONObject()
    private var recordProcess = JSONArray()
    private var recordOutput = JSONArray()

    init {
        execute {
            modelList.addAll(kotlin.runCatching {
                aiService.getChatModels()
            }.onFailure { it.printStackTrace() }.getOrDefault(listOf()))

            if (modelList.isEmpty()) return@execute

            chatBot = (modelList.find { it.chatBotId == selectedModelId } ?: modelList[0]).let {
                ModelChatBot(it)
            }
        }
    }

    fun initArgs(id:String) {
        this.transId = id

        val v = DataHolder.get<String>(id)
        // TODO only for test
        if (v.isNullOrBlank()) {
            // 如果没有传，那么从数据库中加载
            viewModelScope.launch(Dispatchers.IO) {
                task = dao.getById(id)
                task?.let {
                    sourceText = it.sourceText
                    resultText = it.resultText
                    translatedLength = it.translatedLength
                    prompt = it.prompt
                    totalLength = it.sourceText.length
                    // TODO chatBot 选择
                    allCorpus.addAll(it.allCorpus)
                    sourceTextSegments.addAll(it.sourceTextSegments)
                    resultTextSegments.addAll(it.resultTextSegments)

                    if (translatedLength > 0) {
                        if (translatedLength == totalLength) {
                            screenState = ScreenState.Result
                        } else {
                            screenState = ScreenState.Translating
                            isPausing = true
                            lastResultText = resultText
                        }
                    }
                }
            }
        } else {
            this.sourceText = v
            this.totalLength = this.sourceText.length
            DataHolder.remove(id)
        }

    }

    fun startTranslate() {
        screenState = ScreenState.Translating
        if (record) {
            recordObj.put("sourceText", sourceText)
        }
        translateJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                while (isActive && translatedLength < totalLength) {
                    if (isEditingTerm) {
                        isPausing = true
                        appCtx.toastOnUi(string(R.string.paused_due_to_editting))
                    }
                    while (isPausing) { delay(100) }

                    // 前文的最后一小点，供上下文衔接
                    val prevEnd = if (lastResultText.isNotEmpty()) {
                        val lastLine = lastResultText.lastIndexOf('\n')
                        if (lastLine == -1 || lastLine == lastResultText.lastIndex)
                            lastResultText.takeLast(50)
                        else lastResultText.substring(lastLine + 1)
                    } else ""
                    val (part, messages) = getNextPart(prevEnd)
                    Log.d(TAG, "startTranslate: nextPart: $part")
                    if (part == "") break
                    translatePart(part, messages)
                }
                delay(500)
                screenState = ScreenState.Result
                if (record) {
                    recordObj.put("process", recordProcess)
                    saveRecord()
                }
                Log.d(TAG, "finishTranslate, sourceStringSegments: $sourceTextSegments, resultTextSegments: $resultTextSegments")
            } catch (e: Exception) {
                Log.e(TAG, "startTranslate: ", e)
                appCtx.toastOnUi(e.displayMsg(string(R.string.translate)))
            }
        }
    }

    fun generateBothExportedText(): String {
        // 根据两个 Segments，生成源文本、翻译结果对照的文本
        if (sourceTextSegments.size != resultTextSegments.size) {
            Log.e(TAG, "generateBothExportedText: sourceTextSegments.size != resultTextSegments.size")
            return ""
        }
        return buildString {
            var lastSourceIndex = 0
            var lastResultIndex = 0
            for (i in sourceTextSegments.indices) {
                val sourceIndex = sourceTextSegments[i]
                val resultIndex = resultTextSegments[i]
                append(sourceText.substring(lastSourceIndex, sourceIndex + 1))
                append("\n")
                append(resultText.substring(lastResultIndex, resultIndex + 1))
                append("\n\n")
                lastSourceIndex = sourceIndex + 1
                lastResultIndex = resultIndex + 1
            }
        }
    }

    /**
     * 获取下一次要翻译的部分
     * 规则：
     * chatBot.maxContextLength 是一次能接受的最大长度
     * 它包括 SystemPrompt + 这次输入 format 后 + 输出的长度 +
     * @return String
     */
    private suspend fun getNextPart(prevEnd: String): Pair<String, ArrayList<ChatMessageReq>> {
        if (translatedLength >= totalLength) return "" to arrayListOf()

        // 最大的输入长度： 模型最大长度 * 0.8
        // 由于模型的输出长度实际远小于上下文长度（gpt3.5、gpt4都只有4096），这里乘以 0.8 以尽量使得输出能输出完
        val maxLength = (chatBot.model.maxOutputTokens * 0.8f).toInt()
        val tokenCounter = chatBot.tokenCounter

        val messages = arrayListOf<ChatMessageReq>()
        // 把上次翻译的最后一点加上
        if (prevEnd.isNotEmpty()) {
            messages.add(ChatMessageReq.text(prevEnd, "assistant"))
        }

        val remainText = sourceText.safeSubstring(translatedLength, translatedLength + maxLength)
        Log.d(TAG, "getNextPart: remainText: ${remainText.abstract()}")
        val text = tokenCounter.truncate(remainText, emptyArray(), maxLength)
        Log.d(TAG, "getNextPart: truncated text: ${text.abstract()}")

        val sb = StringBuilder(text)

        // 寻找当前的 corpus
        // TODO 改成更合理的方式，比如基于 NLP 的分词
        currentCorpus.clear()
        val needToAddTerms = mutableSetOf<Term>()
        allCorpus.list.forEach {
            if (text.contains(it.first)) {
                needToAddTerms.add(it)
            }
        }
        currentCorpus.addAll(needToAddTerms)
        Log.d(TAG, "getNextPart: allCorpus: $allCorpus, currentCorpus: $currentCorpus")

        if (needToAddTerms.isNotEmpty()) {
            sb.append(SEP)
            sb.append(JSONArray(needToAddTerms).toString())
        }
        messages.add(ChatMessageReq.text(sb.toString(), "user"))
        return text to messages
    }

    private suspend fun translatePart(part: String, messages: List<ChatMessageReq>) {
        /**
         * 处理错误相关的逻辑，返回值为是否达到了最大值而弹出 toast
         * @return Boolean
         */
        fun onError(): Boolean {
            errorTimes++
            if (errorTimes == 3) {
                isPausing = true
                context.toastOnUi(R.string.translate_paused_too_many_retries)
                translateJob?.cancel()
                translateJob = null
                return true
            }
            return false
        }

        currentOutput.clear()
        currentTransPartLength = part.length

        val systemPrompt = prompt.toPrompt()
        val maxOutputTokens = chatBot.model.maxOutputTokens
        val chatMessages = messages.map { newChatMessage(it.role, it.content) }
        val args = mapOf("max_tokens" to maxOutputTokens)
        chatBot.chat(transId, part, chatMessages, systemPrompt, memory, args).collect { streamMsg ->
            when(streamMsg) {
                is StreamMessage.Start -> {
                    if (record) {
                        recordOutput = JSONArray()
                    }
                }
                is StreamMessage.Part -> {
                    try {
                        currentOutput.append(streamMsg.part)
                        val ans = parseStreamedOutput(currentOutput.toString())
                        resultText = lastResultText + (ans.text ?: "")
                        ans.keywords?.forEach {
                            allCorpus.add(it[0] to it[1])
                        }
                        saveAllCorpusToDB()

                        if (record) {
                            recordOutput.put(streamMsg.part)
                        }
                    } catch (e: JsonParseException) {
                        // JSON 数据解析失败了，谈个提示，报个错
                        // 继续往下走，后面能不能解析出来
                        if (!onError()) { // 如果已经暂停了，就不弹出这一段了
                            appCtx.toastOnUi(string(R.string.attemp_to_retry))
                        }
                        Log.w(TAG, "translatePart: 刚刚那一段解析失败了:\n$currentOutput", )
                    }
                }
                is StreamMessage.End -> {
                    translatedLength += part.length
                    lastResultText = resultText

                    sourceTextSegments.add(translatedLength - 1)
                    resultTextSegments.add(lastResultText.length - 1)

                    saveToDB()

                    if (record) {
                        recordProcess.put(JSONObject().apply {
                            put("endIndex", translatedLength - 1)
                            put("output", recordOutput)
                        })
                    }
                }
                is StreamMessage.Error -> {
                    if (!onError()) {
                        appCtx.toastOnUi(streamMsg.error)
                        delay(1000)
                    }
                }
                else -> Unit
            }
        }
    }

    private fun saveToDB() {
        dbAction {
            task = LongTextTransTask(
                id = transId,
                chatBotId = chatBot.id,
                sourceText = sourceText,
                resultText = resultText,
                prompt = prompt,
                allCorpus = allCorpus.toList(),
                sourceTextSegments = sourceTextSegments,
                resultTextSegments = resultTextSegments,
                translatedLength = translatedLength
            )
            dao.upsert(task!!)
        }
    }

    private fun saveAllCorpusToDB() {
        dbAction {
            dao.updateAllCorpus(transId, allCorpus.toList())
        }
    }

    private inline fun dbAction(crossinline action: () -> Unit) {
        dbJob?.cancel()
        dbJob = viewModelScope.launch(Dispatchers.IO) {
            action()
        }
    }


    /**
     * 尝试解析流式的 JSON 数据
     * @param part String
     */
    private fun parseStreamedOutput(text: String): Answer {
        val idx = text.lastIndexOf(SEP)
        if (idx == -1 || idx == text.length - SEP.length) {
            return Answer(text)
        }

        val list = PartialJsonParser.parse(text.substring(idx + SEP.length)) as? List<*>
        val keywords = list?.filter { it is List<*> && it.size == 2 } as? List<List<String>>

        return Answer(
            text = text.substring(0, idx),
            // 由于解析得到的 keywords 不一定满足要求（比如每一项长度为 2），这里处理一下
            keywords = keywords ?: arrayListOf()
        )
    }

    private fun newChatMessage(sender: String, msg: String): ChatMessage {
        return ChatMessage(botId = chatBot.id, conversationId = transId, sender = if (sender == "user") SENDER_ME else sender, content = msg)
    }

    fun updatePrompt(prefix: String) { prompt = prompt.copy(prefix = prefix) }
    fun resetPrompt() { prompt = DEFAULT_PROMPT }
    fun updateEditingTermState(isEditing: Boolean) { isEditingTerm = isEditing }
    fun updateSourceText(text: String) { sourceText = text; totalLength = text.length }
    fun updateBot(model: Model) {
        chatBot = ModelChatBot(model)
    }
    fun updateRemark(taskId: String, remark: String) {
        task ?: return
        dbAction {
            task = task!!.copy(remark = remark)
            dao.updateRemark(taskId, remark)
        }
    }

    fun toggleIsPausing() {
        isPausing = !isPausing
        if (isPausing) appCtx.toastOnUi(string(R.string.paused_tip))
        else {
            if (translateJob == null) {
                // 如果没有开始翻译（从外部加载进来的状态），那么开始翻译
                startTranslate()
            }
            errorTimes = 0
        }
    }

    fun retryCurrentPart() {
        isPausing = false
        errorTimes = 0
        startTranslate()
    }

    private fun saveRecord() {
        if (record) {
            viewModelScope.launch(Dispatchers.IO) {
                val file = appCtx.externalCacheDir?.resolve("record_${System.currentTimeMillis()}.json")
                file?.writeText(recordObj.toString(2))
            }
        }
    }

    companion object {
        private const val TAG = "LongTextTransViewModel"
    }
}

private fun String.abstract() =
    if (length > 30) substring(0, 15) + "..." + takeLast(15) else this + "(${length})"