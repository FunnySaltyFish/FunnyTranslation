@file:OptIn(ExperimentalCoroutinesApi::class)

package com.funny.translation.translate.ui.main

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.funny.translation.Consts
import com.funny.translation.TranslateConfig
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.js.JsEngine
import com.funny.translation.js.core.JsTranslateTaskText
import com.funny.translation.translate.*
import com.funny.translation.translate.database.DefaultData
import com.funny.translation.translate.database.TransHistoryBean
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.engine.TextTranslationEngines
import com.funny.translation.translate.utils.SortResultUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MainViewModel : ViewModel() {
    var translateText by mutableStateOf("")
    private val actualTransText: String
        get() = translateText.trim().replace("#","")

    var sourceLanguage by mutableStateOf(
        findLanguageById(
            DataSaverUtils.readData(
                Consts.KEY_SOURCE_LANGUAGE,
                Language.ENGLISH.id
            )
        )
    )
    var targetLanguage by mutableStateOf(
        findLanguageById(
            DataSaverUtils.readData(
                Consts.KEY_TARGET_LANGUAGE,
                Language.CHINESE.id
            )
        )
    )

    var selectedEngines: HashSet<TranslationEngine> = hashSetOf()
    private var initialSelected = 0

    private var jsEngineInitialized = false

    var showListType: ShowListType by mutableStateOf(ShowListType.History)

    val jsEnginesFlow : Flow<List<JsTranslateTaskText>> = appDB.jsDao.getEnabledJs().mapLatest { list ->
        list.map {
            JsTranslateTaskText(jsEngine = JsEngine(jsBean = it)).apply {
                this.selected = DataSaverUtils.readData(this.selectKey, false)
                if(this.selected){
                    addSelectedEngines(this)
                    initialSelected++
                }
                Log.d(ActivityViewModel.TAG, "${this.jsEngine.jsBean.fileName} selected:$selected ")
            }
        }.sortedBy(SortResultUtils.defaultEngineSort).also { jsEngineInitialized = true }
    }

    val bindEnginesFlow = DefaultData.bindEngines.map {
        it.selected = DataSaverUtils.readData(it.selectKey, false)
        if(it.selected) {
            addSelectedEngines(it)
            initialSelected++
        }
        it
    }.sortedBy(SortResultUtils.defaultEngineSort).let {
        flowOf(it)
    }

//    val transHistoryPagingSource = TransHistoryPagingSource(appDB.transHistoryDao)

    val transHistories = Pager(PagingConfig(pageSize = 10)) {
        appDB.transHistoryDao.queryAll()
    }.flow.cachedIn(viewModelScope)

//    val

//    private val localEnginesFlow : Flow<List<TranslationEngine>>
//        get() = bindEnginesFlow.combine(jsEnginesFlow){ bindEngines, jsEngines ->
//            (bindEngines + jsEngines)
//        }
//            .flowOn(Dispatchers.IO).conflate()

    init {
        viewModelScope.launch {
            // 延时，等待插件加载完
            while (!jsEngineInitialized) {
                delay(100)
            }
            if(initialSelected == 0) {
                // 默认选两个
                TextTranslationEngines.BaiduNormal.selected = true
                TextTranslationEngines.Youdao.selected = true

                DataSaverUtils.saveData(TextTranslationEngines.BaiduNormal.selectKey, true)
                DataSaverUtils.saveData(TextTranslationEngines.Youdao.selectKey, true)

                addSelectedEngines(TextTranslationEngines.BaiduNormal, TextTranslationEngines.Youdao)
            }
        }
    }


    val resultList = mutableStateListOf<TranslationResult>()

    var progress by mutableStateOf(100f)

    private val totalProgress: Int
        get() = selectedEngines.size

    private var translateJob: Job? = null

    fun addSelectedEngines(vararg engines: TranslationEngine) {
        Log.d(TAG, "addSelectedEngines: ${engines.joinToString{it.name}}")
        selectedEngines.addAll(engines)
    }

    fun removeSelectedEngine(engine: TranslationEngine) {
        selectedEngines.remove(engine)
    }

    fun cancel() {
        translateJob?.cancel()
        progress = 100f
    }

    fun isTranslating(): Boolean = translateJob?.isActive ?: false

    fun translate() {

        if (translateJob?.isActive == true) return
        if (actualTransText.isEmpty()) return

        resultList.clear()
        progress = 0f
        addTransHistory(actualTransText, sourceLanguage, targetLanguage)
        showListType = ShowListType.Result
        translateJob = viewModelScope.launch {
            // 延时，等待插件加载完
            while (!jsEngineInitialized) {
                delay(100)
            }
            createFlow().buffer().collect { task ->
                try {
                    with(TranslateConfig){
                        this.sourceLanguage = task.sourceLanguage
                        this.targetLanguage = task.targetLanguage
                        this.sourceString   = task.sourceString
                    }

                    task.result.targetLanguage = targetLanguage
                    withContext(Dispatchers.IO) {
                        task.translate()
                    }

                    updateTranslateResult(task.result)
                    Log.d(TAG, "translate : $progress ${task.result}")

                } catch (e: TranslationException) {
                    e.printStackTrace()
                    with(task.result) {
                        setBasicResult(
                            "${FunnyApplication.resources.getString(R.string.error_result)}\n${e.message}"
                        )
                        updateTranslateResult(this)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    with(task.result) {
                        setBasicResult(FunnyApplication.resources.getString(R.string.error_result))
                        updateTranslateResult(this)
                    }
                }
            }
        }
    }

    private fun addTransHistory(sourceString: String, sourceLanguage: Language, targetLanguage: Language){
        viewModelScope.launch(Dispatchers.IO) {
            appDB.transHistoryDao.insertTransHistory(
                TransHistoryBean(0, sourceString, sourceLanguage.id, targetLanguage.id, selectedEngines.map { it.name })
            )
        }
    }

    fun deleteTransHistory(sourceString: String){
        viewModelScope.launch(Dispatchers.IO) {
            appDB.transHistoryDao.deleteTransHistoryByContent(sourceString)
        }
    }

    private fun createFlow() =
        flow {
            selectedEngines.forEach {
                if (support(it.supportLanguages)) {
                    val task = if (it is TextTranslationEngines) {
                        it.createTask(
                            actualTransText,
                            sourceLanguage,
                            targetLanguage
                        )
                    } else {
                        val jsTask = it as JsTranslateTaskText
                        jsTask.sourceString = actualTransText
                        jsTask.sourceLanguage = sourceLanguage
                        jsTask.targetLanguage = targetLanguage
                        jsTask
                    }
                    emit(task)
                } else {
                    val result = TranslationResult(it.name).apply {
                        setBasicResult("当前引擎暂不支持该语种！")
                    }
                    updateTranslateResult(result)
                }
            }
        }

    private fun updateTranslateResult(result: TranslationResult) {
        progress += 100f / totalProgress
        resultList.let {
            val currentKey = it.find { r -> r.engineName == result.engineName }
            // 绝大多数情况下应该是没有的
            // 但是线上的报错显示有时候会有，所以判断一下吧
            if (currentKey != null) it.remove(currentKey)
            it.add(result)
            it.sortBy(SortResultUtils.defaultResultSort)
        }
    }

    private fun support(supportLanguages: List<Language>) =
        supportLanguages.contains(sourceLanguage) && supportLanguages.contains(targetLanguage)


    companion object {
        private const val TAG = "MainVM"
    }
}