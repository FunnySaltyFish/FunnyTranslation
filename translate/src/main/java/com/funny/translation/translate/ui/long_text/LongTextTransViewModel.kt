package com.funny.translation.translate.ui.long_text

import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.compose.ai.ServerChatBot
import com.funny.compose.ai.TestLongTextChatBot
import com.funny.compose.ai.bean.ChatMemoryMaxContextSize
import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.bean.SENDER_ME
import com.funny.compose.ai.bean.StreamMessage
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.helper.JsonX
import com.funny.translation.helper.PartialJsonParser
import com.funny.translation.helper.TextSplitter
import com.funny.translation.helper.string
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.Language
import com.funny.translation.translate.R
import com.funny.translation.translate.appCtx
import com.funny.translation.translate.utils.DataHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
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
    internal var screenState by mutableStateOf(ScreenState.Init)

    val chatBot: ServerChatBot = TestLongTextChatBot()
    var totalLength = 0
    var translatedLength by mutableIntStateOf(0)

    val progress by derivedStateOf {  if (translatedLength == 0) 0f else (translatedLength.toFloat() / totalLength).coerceIn(0f, 1f) }
    val startedProgress by derivedStateOf {  if (translatedLength == 0) 0f else ((translatedLength + currentTransPartLength).toFloat() / totalLength).coerceIn(0f, 1f) }

    private var translateJob: Job? = null
    // 是否正在编辑术语，是的话暂停一下翻译
    private var isEditingTerm: Boolean = false
    // 是否暂停
    var isPausing by mutableStateOf(false)

    var transId by mutableStateOf(UUID.randomUUID().toString())
    var histories = mutableListOf<ChatMessage>()
    internal var prompt by mutableStateOf(DEFAULT_PROMPT)
    var memory = ChatMemoryMaxContextSize(1024, prompt.toPrompt())
    val allCorpus = TermList()
    val currentCorpus = TermList()

    var sourceString by mutableStateOf("")
    var sourceLanguage by mutableDataSaverStateOf(DataSaverUtils, "key_source_lang", Language.ENGLISH)
    var targetLanguage by mutableDataSaverStateOf(DataSaverUtils, "key_target_lang", Language.CHINESE)
    var resultText by mutableStateOf("")

    // 当前 part 翻译得到的结果
    private var resultJsonPart = StringBuilder()
    // 已经完成的 parts 翻译得到的结果
    private var lastResultText = ""
    var currentTransPartLength = 0 // 当前翻译的长度
    val currentResultStartOffset get() = lastResultText.length

    fun initArgs(id:String, sourceStringKey: String) {
        this.transId = id

        val v = DataHolder.get<String>(sourceStringKey)
        // TODO only for test
        if (v.isNullOrBlank()) {
            this.sourceString = List(100) { "(${it}) XiaoMing told XiaoHong, the best student in this class is XiaoZhang." }.joinToString(separator = " ")
            this.allCorpus.addAll(arrayOf("XiaoHong" to "萧红", "XiaoMing" to "晓明"))
        } else {
            this.sourceString = v
        }
        this.totalLength = this.sourceString.length
    }

    fun startTranslate() {
        Log.d(TAG, "startTranslate: args: totalLength: $totalLength, sourceLanguage: $sourceLanguage, targetLanguage: $targetLanguage")
        screenState = ScreenState.Translating
        translateJob = viewModelScope.launch(Dispatchers.IO) {
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
            sourceString.substring(translatedLength, minOf(translatedLength + maxLength, totalLength)),
            remainLength
        )
        return splitText
    }

    private suspend fun translatePart(part: String) {
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
                is StreamMessage.Part -> {
                    resultJsonPart.append(streamMsg.part)
                    val ans = parseStreamedJson(resultJsonPart.toString())
                    Log.d(TAG, "translatePart: ans: $ans")
                    if (ans != null) {
                        resultText = lastResultText + (ans.text ?: "")
                        ans.keywords?.forEach {
                            allCorpus.add(it[0] to it[1])
                        }
                    }
                }
                is StreamMessage.End -> {
                    translatedLength += part.length
                    lastResultText = resultText
                }
                else -> Unit
            }
        }
    }


    /**
     * 尝试解析流式的 JSON 数据
     * @param part String
     */
    private fun parseStreamedJson(text: String): Answer? {
        try {
            val completeJson = PartialJsonParser.completePartialJson(text)
            Log.d(TAG, "parseStreamedJson: --- origin: $text\n--- parsed: $completeJson")
            val ans: Answer = JsonX.fromJson(completeJson)
            // 由于解析得到的 keywords 不一定满足要求（比如每一项长度为 2），这里处理一下
            if (ans.keywords != null) {
                ans.keywords = ans.keywords!!.filter { it.size == 2 }
            }
            return ans
        } catch (e: Exception) {
            // 处理异常
            Log.e(TAG, "parseStreamedJson: ", e)
        }
        return null
    }

    private fun newChatMessage(msg: String): ChatMessage {
        return ChatMessage(UUID.randomUUID().toString(), chatBot.id, transId, SENDER_ME, msg)
    }

    fun updateSourceLanguage(language: Language) { sourceLanguage = language }
    fun updateTargetLanguage(language: Language) { targetLanguage = language }
    fun updatePrompt(prefix: String) { prompt.prefix = prefix }
    fun updateEditingTermState(isEditing: Boolean) { isEditingTerm = isEditing }
    fun toggleIsPausing() {
        isPausing = !isPausing
        if (isPausing) appCtx.toastOnUi(string(R.string.paused_tip))
    }

    companion object {
        private const val TAG = "LongTextTransViewModel"
    }
}