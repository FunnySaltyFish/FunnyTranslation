package com.funny.translation.translate.ui.main

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.translation.TranslateConfig
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.js.JsEngine
import com.funny.translation.js.core.JsTranslateTask
import com.funny.translation.trans.*
import com.funny.translation.translate.ActivityViewModel
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.R
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.database.DefaultData
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.engine.TranslationEngines
import com.funny.translation.translate.utils.SortResultUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.URLEncoder

class MainViewModel : ViewModel() {
    val translateText = MutableLiveData("")
    private val actualTransText: String
        get() = translateText.value?.trim()?.replace("#","") ?: ""

    val sourceLanguage: MutableLiveData<Language> = MutableLiveData(
        findLanguageById(
            DataSaverUtils.readData(
                Consts.KEY_SOURCE_LANGUAGE,
                Language.ENGLISH.id
            )
        )
    )
    val targetLanguage: MutableLiveData<Language> = MutableLiveData(
        findLanguageById(
            DataSaverUtils.readData(
                Consts.KEY_TARGET_LANGUAGE,
                Language.CHINESE.id
            )
        )
    )
    private val translateMode: MutableLiveData<Int> = MutableLiveData(0)

    var selectedEngines: HashSet<TranslationEngine> = hashSetOf()
    private var initialSelected = 0

    val jsEnginesFlow : Flow<List<JsTranslateTask>> = appDB.jsDao.getEnabledJs().mapLatest { list ->
        list.map {
            JsTranslateTask(jsEngine = JsEngine(jsBean = it)).apply {
                this.selected = DataSaverUtils.readData(this.selectKey, false)
                if(this.selected){
                    addSelectedEngines(this)
                    initialSelected++
                }
                Log.d(ActivityViewModel.TAG, "${this.jsEngine.jsBean.fileName} selected:$selected ")
            }
        }.sortedBy(SortResultUtils.defaultEngineSort)
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

//    private val localEnginesFlow : Flow<List<TranslationEngine>>
//        get() = bindEnginesFlow.combine(jsEnginesFlow){ bindEngines, jsEngines ->
//            (bindEngines + jsEngines)
//        }
//            .flowOn(Dispatchers.IO).conflate()

    init {
        viewModelScope.launch {
            // 延时一秒，等待插件加载完
            delay(1000)
            if(initialSelected == 0) {
                // 默认选两个
                TranslationEngines.BaiduNormal.selected = true
                TranslationEngines.Youdao.selected = true

                DataSaverUtils.saveData(TranslationEngines.BaiduNormal.selectKey, true)
                DataSaverUtils.saveData(TranslationEngines.Youdao.selectKey, true)

                addSelectedEngines(TranslationEngines.BaiduNormal, TranslationEngines.Youdao)
            }
        }
    }


    val resultList: MutableLiveData<ArrayList<TranslationResult>> = MutableLiveData(arrayListOf())

    val progress: MutableLiveData<Float> = MutableLiveData(100f)

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
        progress.value = 100f
    }

    fun isTranslating(): Boolean = translateJob?.isActive ?: false

    fun translate() {
        if (translateJob?.isActive == true) return
        if (actualTransText.isEmpty()) return
        resultList.value?.clear()
        progress.value = 0f
        translateJob = viewModelScope.launch {
            createFlow().buffer().collect { task ->
                try {
                    with(TranslateConfig){
                        this.sourceLanguage = task.sourceLanguage
                        this.targetLanguage = task.targetLanguage
                        this.sourceString   = task.sourceString
                    }

                    task.result.targetLanguage = targetLanguage.value!!
                    withContext(Dispatchers.IO) {
                        task.translate(translateMode.value!!)
                    }

                    updateTranslateResult(task.result)
                    Log.d(TAG, "translate : ${progress.value} ${task.result}")

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

    private fun createFlow() =
        flow {
            selectedEngines.forEach {
                if (support(it.supportLanguages)) {
                    val task = if (it is TranslationEngines) {
                        it.createTask(
                            actualTransText,
                            sourceLanguage.value!!,
                            targetLanguage.value!!
                        )
                    } else {
                        val jsTask = it as JsTranslateTask
                        jsTask.sourceString = actualTransText
                        jsTask.sourceLanguage = sourceLanguage.value!!
                        jsTask.targetLanguage = targetLanguage.value!!
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
        progress.value = progress.value!! + 100f / totalProgress
        resultList.value?.let {
            val currentKey = it.find { r -> r.engineName == result.engineName }
            // 绝大多数情况下应该是没有的
            // 但是线上的报错显示有时候会有，所以判断一下吧
            if (currentKey != null) it.remove(currentKey)
            it.add(result)
            it.sortBy(SortResultUtils.defaultResultSort)
        }
    }

    private fun support(supportLanguages: List<Language>) =
        supportLanguages.contains(sourceLanguage.value) && supportLanguages.contains(targetLanguage.value)


    companion object {
        private const val TAG = "MainVM"
    }
}