@file:OptIn(ExperimentalCoroutinesApi::class)

package com.funny.translation.translate.ui.main

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.funny.data_saver.core.mutableDataSaverStateOf
import com.funny.translation.AppConfig
import com.funny.translation.Consts
import com.funny.translation.TranslateConfig
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.js.JsEngine
import com.funny.translation.js.core.JsTranslateTaskText
import com.funny.translation.translate.*
import com.funny.translation.translate.database.DefaultData
import com.funny.translation.translate.database.TransHistoryBean
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.engine.TextTranslationEngine
import com.funny.translation.translate.engine.TextTranslationEngines
import com.funny.translation.translate.engine.selectKey
import com.funny.translation.translate.utils.SortResultUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MainViewModel : ViewModel() {
    var translateText by mutableStateOf("")
    val actualTransText: String
        get() = translateText.trim().replace("#","").replace("&", "")

    var sourceLanguage by mutableDataSaverStateOf(
        DataSaverUtils,
        "key_source_lang",
        findLanguageById(
            DataSaverUtils.readData(
                Consts.KEY_SOURCE_LANGUAGE,
                Language.ENGLISH.id
            )
        )
    )

    var targetLanguage by mutableDataSaverStateOf(
        DataSaverUtils,
        "key_target_lang",
        findLanguageById(
            DataSaverUtils.readData(
                Consts.KEY_TARGET_LANGUAGE,
                Language.CHINESE.id
            )
        )
    )

    fun updateSourceLanguage(language: Language){ sourceLanguage = language }
    fun updateTargetLanguage(language: Language){ targetLanguage = language }


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
                Log.d(TAG, "${this.jsEngine.jsBean.fileName} selected:$selected ")
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
        MutableStateFlow(it)
    }

    val transHistories = Pager(PagingConfig(pageSize = 10)) {
        appDB.transHistoryDao.queryAllPaging()
    }.flow.cachedIn(viewModelScope)

    private val mutex by lazy(LazyThreadSafetyMode.PUBLICATION) { Mutex() }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // 随应用升级，有一些插件可能后续转化为内置引擎，旧的插件需要删除
            appDB.jsDao.getAllJs().forEach { jsBean ->
                if(DefaultData.isPluginBound(jsBean)) {
                    appDB.jsDao.deleteJs(jsBean)
                }
            }

            // 延时，等待插件加载完
            while (!jsEngineInitialized) {
                Log.d(TAG, "init: wait jsEngineInitialized")
                delay(100)
            }

            Log.d(TAG, "initial selected: $initialSelected")
            if(initialSelected == 0) {
                // 默认选两个
                addDefaultEngines(TextTranslationEngines.BaiduNormal, TextTranslationEngines.Tencent)
            }
        }
    }


    val resultList = mutableStateListOf<TranslationResult>()

    var progress by mutableStateOf(100f)

    private val totalProgress: Int
        get() = selectedEngines.size

    private var translateJob: Job? = null

    /**
     * 当什么都不选时，添加默认的引擎
     * @param engines Array<out TextTranslationEngines>
     */
    private fun addDefaultEngines(vararg engines: TextTranslationEngines) {
        selectedEngines.addAll(engines)
        engines.forEach {
            it.selected = true
            DataSaverUtils.saveData(it.selectKey, true)
        }
        viewModelScope.launch {
            val oldBindEngines = bindEnginesFlow.value
            val newBindEngines = oldBindEngines.map {
                engines.find { engine -> engine.name == it.name } ?: it
            }
            bindEnginesFlow.emit(newBindEngines)
        }
    }

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

            TranslateConfig.sourceLanguage = sourceLanguage
            TranslateConfig.targetLanguage = targetLanguage
            TranslateConfig.sourceString =   actualTransText
            if (AppConfig.sParallelTrans.value) {
                translateInParallel()
                Log.d(TAG, "translate: translateInParallel finished")
            } else {
                translateInSequence()
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

    private suspend fun translateInSequence(){
        createFlow().buffer().collect { task ->
            try {
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

    private suspend fun translateInParallel() {
        val tasks: ArrayList<Deferred<*>> = arrayListOf()
        createFlow(true).buffer().collect { task ->
            tasks.add(viewModelScope.async {
                try {
                    mutex.withLock {
                        task.result.targetLanguage = targetLanguage
                    }
                    withContext(Dispatchers.IO) {
                        task.translate()
                    }
                    updateTranslateResultWithMutex(task.result)
                    Log.d(TAG, "translate : $progress ${task.result}")
                } catch (e: TranslationException) {
                    e.printStackTrace()
                    mutex.withLock {
                        with(task.result) {
                            setBasicResult(
                                "${FunnyApplication.resources.getString(R.string.error_result)}\n${e.message}"
                            )
                            updateTranslateResult(this)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    mutex.withLock {
                        with(task.result) {
                            setBasicResult(FunnyApplication.resources.getString(R.string.error_result))
                            updateTranslateResult(this)
                        }
                    }
                }
            })
        }
        // 等待所有任务完成再返回，使对翻译状态的判断正常
        tasks.awaitAll()
    }

    private fun createFlow(withMutex: Boolean = false) =
        flow {
            selectedEngines.forEach {
                if (support(it.supportLanguages)) {
                    val lambda = {
                        val task = if (it is TextTranslationEngines) {
                            it.createTask(
                                actualTransText,
                                sourceLanguage,
                                targetLanguage
                            )
                        } else {
                            val jsTask = it as JsTranslateTaskText
                            jsTask.result.engineName = jsTask.name
                            jsTask.sourceString = actualTransText
                            jsTask.sourceLanguage = sourceLanguage
                            jsTask.targetLanguage = targetLanguage
                            jsTask
                        }
                        if (withMutex) task.mutex = mutex
                        task
                    }
                    if (withMutex) {
                        mutex.withLock {
                            emit(lambda())
                        }
                    } else {
                        emit(lambda())
                    }
                } else {
                    val lambda = {
                        val result = TranslationResult(it.name).apply {
                            setBasicResult("当前引擎暂不支持该语种！")
                        }
                        updateTranslateResult(result)
                    }
                    if (withMutex) {
                        mutex.withLock {
                            lambda()
                        }
                    } else {
                        lambda()
                    }
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
        if (showListType != ShowListType.Result) showListType = ShowListType.Result
    }

    private suspend fun updateTranslateResultWithMutex(result: TranslationResult) {
        mutex.withLock {
            updateTranslateResult(result)
        }
    }

    private fun support(supportLanguages: List<Language>) =
        supportLanguages.contains(sourceLanguage) && supportLanguages.contains(targetLanguage)


    companion object {
        private const val TAG = "MainVM"
    }
}