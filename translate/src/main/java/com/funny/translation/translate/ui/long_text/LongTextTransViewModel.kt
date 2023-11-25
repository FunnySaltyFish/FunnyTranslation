package com.funny.translation.translate.ui.long_text

import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.compose.ai.bean.ChatMemoryMaxContextSize
import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.bean.SENDER_ME
import com.funny.compose.ai.bean.StreamMessage
import com.funny.compose.ai.chat.ChatBots
import com.funny.compose.ai.chat.ServerChatBot
import com.funny.compose.ai.chat.TestLongTextChatBot
import com.funny.translation.helper.DataHolder
import com.funny.translation.helper.JsonX
import com.funny.translation.helper.PartialJsonParser
import com.funny.translation.helper.TextSplitter
import com.funny.translation.helper.displayMsg
import com.funny.translation.helper.string
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.R
import com.funny.translation.translate.appCtx
import com.funny.translation.translate.database.LongTextTransTask
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.ui.long_text.bean.TermList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import java.util.UUID


const val DEFAULT_PROMPT_PREFIX = """你现在是一名优秀的翻译人员，现在在翻译长文中的某个片段。请根据给定的术语表，将输入文本翻译成中文"""
const val DEFAULT_PROMPT_SUFFIX = """，并找出可能存在的新术语，以JSON的形式返回（如果没有，请返回[]）。
示例输入：{"text":"XiaoHong and XiaoMing are studying DB class.","keywords":[["DB","数据库"],["XiaoHong","萧红"]]}
示例输出：{"text":"萧红和晓明正在学习数据库课程","keywords":[["XiaoMing","晓明"]]}。
你的输出必须为JSON格式"""

private val DEFAULT_PROMPT = EditablePrompt(DEFAULT_PROMPT_PREFIX, DEFAULT_PROMPT_SUFFIX)


@Serializable
private data class Answer(val text: String? = null, var keywords: List<List<String>>? = null)

internal enum class ScreenState {
    Init, Translating, Result
}

class LongTextTransViewModel: ViewModel() {
    private val dao = appDB.longTextTransDao
    internal var task: LongTextTransTask? = null

    internal var screenState by mutableStateOf(ScreenState.Init)

    var chatBot: ServerChatBot by mutableStateOf(TestLongTextChatBot())
    var totalLength = 0
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
    private var histories = mutableListOf<ChatMessage>()
    internal var prompt by mutableStateOf(DEFAULT_PROMPT)
    private var memory = ChatMemoryMaxContextSize(1024, prompt.toPrompt())

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
    private var resultJsonPart = StringBuilder()
    // 已经完成的 parts 翻译得到的结果
    private var lastResultText = ""
    // 因各种问题导致的重试

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
//            this.sourceText = List(100) { "(${it}) XiaoMing told XiaoHong, the best student in this class is XiaoZhang." }.joinToString(separator = " ")
//            this.allCorpus.addAll(arrayOf("XiaoHong" to "萧红", "XiaoMing" to "晓明"))
        } else {
            this.sourceText = v
            this.totalLength = this.sourceText.length
            DataHolder.remove(id)
        }

    }

    fun startTranslate() {
        screenState = ScreenState.Translating
        translateJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                while (isActive && translatedLength < totalLength) {
                    val part = getNextPart()
                    Log.d(TAG, "startTranslate: nextPart: $part")
                    if (part == "") break
                    if (isEditingTerm) {
                        isPausing = true
                        appCtx.toastOnUi(string(R.string.paused_due_to_editting))
                    }
                    while (isPausing) { delay(100) }
                    translatePart(part)
                }
                delay(500)
                screenState = ScreenState.Result
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
     * 它包括 SystemPrompt + 这次输入 format 后的长度
     * @return String
     */
    private fun getNextPart(): String {
        val maxLength = chatBot.maxContextLength
        val systemPrompt = prompt.toPrompt()
        val remainLength = maxLength - systemPrompt.length - 1 // 换行符
        val splitText = TextSplitter.splitTextNaturally(
            sourceText.substring(translatedLength, minOf(translatedLength + maxLength, totalLength)),
            remainLength
        )
        return splitText
    }

    private suspend fun translatePart(part: String, retryTimes: Int = 0) {
        if (retryTimes >= 3) {
            throw Exception(string(R.string.translate_failed_too_many_retries))
        }
        resultJsonPart.clear()
        histories.add(newChatMessage(part))
        // 寻找当前的 corpus
        // TODO 改成更合理的方式，比如基于 NLP 的分词
        currentCorpus.clear()
        val needToAddTerms = mutableSetOf<Term>()
        allCorpus.list.forEach {
            if (part.contains(it.first)) {
                needToAddTerms.add(it)
            }
        }
        currentCorpus.addAll(needToAddTerms)
        chatBot.args["keywords"] = currentCorpus.toList()
        Log.d(TAG, "translatePart: allCorpus: $allCorpus, currentCorpus: $currentCorpus")
        currentTransPartLength = part.length
        chatBot.chat(transId, part, histories, prompt.toPrompt(), memory).collect { streamMsg ->
            when(streamMsg) {
                is StreamMessage.Start -> {
//                    sourceStringSegments.add(translatedLength)
                }
                is StreamMessage.Part -> {
                    try {
                        resultJsonPart.append(streamMsg.part)
                        val ans = parseStreamedJson(resultJsonPart.toString())
                        Log.d(TAG, "translatePart: ans: $ans")
                        resultText = lastResultText + (ans.text ?: "")
                        ans.keywords?.forEach {
                            allCorpus.add(it[0] to it[1])
                        }
                        saveAllCorpusToDB()
                    } catch (e: SerializationException) {
                        // JSON 数据解析失败了，重新尝试这一段，如果错误达到三次，则停止
                        appCtx.toastOnUi(string(R.string.attemp_to_retry, retryTimes + 1))
                        delay(500)
                        translatePart(part, retryTimes + 1)
                    }
                }
                is StreamMessage.End -> {
                    translatedLength += part.length
                    lastResultText = resultText

                    sourceTextSegments.add(translatedLength - 1)
                    resultTextSegments.add(lastResultText.length - 1)

                    saveToDB()
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
    private fun parseStreamedJson(text: String): Answer {
        val completeJson = PartialJsonParser.completePartialJson(text)
        Log.d(TAG, "parseStreamedJson: --- origin: $text\n--- parsed: $completeJson")
        val ans: Answer = JsonX.fromJson(completeJson)
        // 由于解析得到的 keywords 不一定满足要求（比如每一项长度为 2），这里处理一下
        if (ans.keywords != null) {
            ans.keywords = ans.keywords!!.filter { it.size == 2 }
        }
        return ans
    }

    private fun newChatMessage(msg: String): ChatMessage {
        return ChatMessage(UUID.randomUUID().toString(), chatBot.id, transId, SENDER_ME, msg)
    }

    fun updatePrompt(prefix: String) { prompt.prefix = prefix }
    fun updateEditingTermState(isEditing: Boolean) { isEditingTerm = isEditing }
    fun updateSourceText(text: String) { sourceText = text; totalLength = text.length }
    fun updateBot(id: Int) {
        ChatBots.findById(id)?.let { this.chatBot = it }
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
        else
            if (translateJob == null) {
                // 如果没有开始翻译（从外部加载进来的状态），那么开始翻译
                startTranslate()
            }
    }




    companion object {
        private const val TAG = "LongTextTransViewModel"
    }
}