package com.funny.translation.translate.ui.ai

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.compose.ai.LONG_TEXT_TRANS_PROMPT
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
import com.funny.translation.translate.Language
import com.funny.translation.translate.utils.DataHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.UUID

// 单个术语，源文本：对应翻译
typealias Term = Pair<String, String>

@Serializable
private data class Answer(val text: String?, var keywords: List<List<String>>?)

class LongTextTransViewModel: ViewModel() {
    val chatBot: ServerChatBot = TestLongTextChatBot()
    var totalLength = 0
    var translatedLength by mutableStateOf(0)
    var lastTranslatedLength = 0 // 上个 part 翻译了完了原文的多少
    val progress by derivedStateOf {  if (translatedLength == 0) 0f else translatedLength.toFloat() / totalLength }

    var transId by mutableStateOf(UUID.randomUUID().toString())
    var histories = mutableListOf<ChatMessage>()
    var prompt by mutableStateOf(LONG_TEXT_TRANS_PROMPT)
    var memory = ChatMemoryMaxContextSize(1024, prompt)
    val allCorpus = mutableStateListOf<Term>()
    val currentCorpus = mutableStateListOf<Term>()

    var sourceString by mutableStateOf("")
    var sourceLanguage by mutableDataSaverStateOf(DataSaverUtils, "key_source_lang", Language.ENGLISH)
    var targetLanguage by mutableDataSaverStateOf(DataSaverUtils, "key_target_lang", Language.CHINESE)
    var resultText by mutableStateOf("")
    private var resultJsonPart = StringBuilder()
    // 已经完成的 parts 翻译得到的结果
    private var lastResultText = ""

    fun initArgs(id:String, totalLength: Int, inputFileUri: Uri, sourceStringKey: String) {
        this.transId = id

        val v = DataHolder.get<String>(sourceStringKey)
        // TODO only for test
        if (v.isNullOrBlank()) {
            this.sourceString = "XiaoMing told XiaoHong, the best student in this class is XiaoZhang."
            this.allCorpus.addAll(arrayOf("XiaoHong" to "萧红", "XiaoMing" to "晓明"))
        } else {
            this.sourceString = v
        }
        this.totalLength = this.sourceString.length
    }

    fun addTerm(term: Term) {
        allCorpus.add(term)
    }

    fun removeTerm(term: Term) {
        allCorpus.remove(term)
        currentCorpus.remove(term)
    }

    fun modifyTerm(origin: Term, target: Term) {
        allCorpus.remove(origin)
        allCorpus.add(target)
        currentCorpus.remove(origin)
        currentCorpus.add(target)
    }

    fun startTranslate() {
        Log.d(TAG, "startTranslate: args: totalLength: $totalLength, sourceLanguage: $sourceLanguage, targetLanguage: $targetLanguage")
        viewModelScope.launch(Dispatchers.IO) {
            while (isActive && translatedLength < totalLength) {
                val part = getNextPart()
                Log.d(TAG, "startTranslate: nextPart: $part")
                if (part == "") break
                translatePart(part)
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
        val systemPrompt = prompt
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
        allCorpus.forEach {
            if (part.contains(it.first)) {
                currentCorpus.add(it)
            }
        }
        chatBot.args["keywords"] = currentCorpus.toList()
        Log.d(TAG, "translatePart: allCorpus: ${allCorpus.joinToString()}, currentCorpus: ${currentCorpus.joinToString()}")
        chatBot.chat(transId, part, histories, prompt, memory).collect { streamMsg ->
            when(streamMsg) {
                is StreamMessage.Part -> {
                    resultJsonPart.append(streamMsg.part)
                    val ans = parseStreamedJson(resultJsonPart.toString())
                    Log.d(TAG, "translatePart: ans: $ans")
                    if (ans != null) {
                        resultText = lastResultText + (ans.text ?: "")
                        ans.keywords?.forEach {
                            addTerm(it[0] to it[1])
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

    companion object {
        private const val TAG = "LongTextTransViewModel"
    }
}