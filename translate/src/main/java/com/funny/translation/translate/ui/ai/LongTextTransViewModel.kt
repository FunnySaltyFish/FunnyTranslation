package com.funny.translation.translate.ui.ai

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.compose.ai.ServerChatBot
import com.funny.compose.ai.TestServerChatBot
import com.funny.compose.ai.bean.ChatMemoryFixedLength
import com.funny.compose.ai.bean.ChatMessage
import com.funny.compose.ai.bean.StreamMessage
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.translate.Language
import com.funny.translation.translate.appCtx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

// 单个术语，源文本：对应翻译
typealias Term = Pair<String, String>

class LongTextTransViewModel: ViewModel() {
    val chatBot: ServerChatBot = TestServerChatBot()
    var totalLength = 0
    var translatedLength = 0
    val progress by derivedStateOf {  if (translatedLength == 0) 0f else translatedLength.toFloat() / totalLength }

    var transId by mutableStateOf(UUID.randomUUID().toString())
    var histories = mutableListOf<ChatMessage>()
    var prompt by mutableStateOf("")
    var memory = ChatMemoryFixedLength(chatBot.maxContextLength)
    val allCorpus = mutableSetOf<Term>()
    val currentCorpus = mutableSetOf<Term>()

    var inputFileUri: Uri? = null
    var resultText by mutableStateOf("")

    var sourceLanguage by mutableDataSaverStateOf(DataSaverUtils, "key_source_lang", Language.ENGLISH)
    var targetLanguage by mutableDataSaverStateOf(DataSaverUtils, "key_target_lang", Language.CHINESE)

    fun initArgs(id:String, totalLength: Int, inputFileUri: Uri) {
        this.transId = id

        // TODO only for test
        if (inputFileUri == Uri.EMPTY) {
            val path = appCtx.externalCacheDir?.path + "/test_long_text_trans.txt"
            val file = File(path)
            if (!file.exists()) {
                file.createNewFile()
                file.writeText("小红知道，对于DBA来说，OS是较为独立的。")
            }
            this.inputFileUri = Uri.fromFile(file)
        } else {
            this.inputFileUri = inputFileUri
        }
        this.totalLength = if (totalLength > 0) totalLength else {
//            appCtx.contentResolver.openInputStream(this.inputFileUri!!).use {
//                it?.bufferedReader()?.useLines {  }
//            }
            var characterCount = 0
            appCtx.contentResolver.openInputStream(this.inputFileUri!!)?.use { inputStream ->
                inputStream.bufferedReader().useLines { lines ->
                    characterCount = lines.sumOf { it.length }
                }
            }
            characterCount
        }
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

    private fun getNextPart(): String {
        inputFileUri ?: return ""
        val res = appCtx.contentResolver.openInputStream(inputFileUri!!)?.use {
            val maxLength = chatBot.maxContextLength
            val bytes = CharArray(maxLength) // TODO 改成实际
            val readBytes = it.bufferedReader().read(bytes, translatedLength, maxLength)
            String(bytes, 0, readBytes)
        }
        return res ?: ""
    }

    private fun translatePart(part: String) {
        viewModelScope.launch(Dispatchers.IO) {
            chatBot.chat(transId, part, histories, prompt, memory).collect { streamMesg ->
                when(streamMesg) {
                    is StreamMessage.Part -> {
                        resultText += streamMesg.part
                        translatedLength += part.length
                    }
                    else -> Unit
                }
            }
        }
    }

    fun updateSourceLanguage(language: Language) { sourceLanguage = language }
    fun updateTargetLanguage(language: Language) { targetLanguage = language }

    companion object {
        private const val TAG = "LongTextTransViewModel"
    }
}