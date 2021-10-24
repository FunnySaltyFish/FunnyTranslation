package com.funny.translation.translate.ui.main

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.translation.helper.DataStoreUtils
import com.funny.translation.js.JsEngine
import com.funny.translation.js.core.JsTranslateTask
import com.funny.translation.trans.*
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.R
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.database.appDB
import com.funny.translation.translate.engine.TranslationEngines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
    val translateText = MutableLiveData("")
    private val actualTransText : String
        get() = translateText.value?.trim() ?: ""

    val sourceLanguage : MutableLiveData<Language> = MutableLiveData(findLanguageById(DataStoreUtils.getSyncData(Consts.KEY_SOURCE_LANGUAGE,Language.ENGLISH.id)))
    val targetLanguage : MutableLiveData<Language> = MutableLiveData(findLanguageById(DataStoreUtils.getSyncData(Consts.KEY_TARGET_LANGUAGE,Language.CHINESE.id)))
    val translateMode : MutableLiveData<Int> = MutableLiveData(0)

    val selectedEngines : List<TranslationEngine>
        get() = allEngines.filter { it.selected }

    val bindEngines : MutableLiveData<ArrayList<TranslationEngine>> = MutableLiveData(
        arrayListOf(
            TranslationEngines.BaiduNormal,
            TranslationEngines.Youdao,
            TranslationEngines.Jinshan,

            TranslationEngines.BiggerText,
            TranslationEngines.EachText,
            TranslationEngines.Bv2Av
        )
    )

    val jsEngines : Flow<List<JsTranslateTask>> = appDB.jsDao.getEnabledJs().map { list ->
        list.map {
            JsTranslateTask(jsEngine = JsEngine(jsBean = it)).apply {
                selected = DataStoreUtils.readBooleanData(this.selectKey, false)
            }
        }
    }

    //val jsEngines : MutableLiveData<ArrayList<TranslationEngine>> = MutableLiveData()
    var allEngines : ArrayList<TranslationEngine> = bindEngines.value!!

    val resultList : MutableLiveData<ArrayList<TranslationResult>> = MutableLiveData(arrayListOf())
    private val _resultList : ArrayList<TranslationResult> = arrayListOf()

    val progress : MutableLiveData<Int> = MutableLiveData(0)
    private val totalProgress : Int
        get() = selectedEngines.map { support(it.supportLanguages) }.size

    var translateJob : Job? = null

    init {
        bindEngines.value?.forEach {
            it.selected = DataStoreUtils.readBooleanData(it.selectKey, false)
        }

        bindEngines.value?.find { it.selected } ?: run {
            TranslationEngines.BaiduNormal.selected = true
            TranslationEngines.Youdao.selected = true
        }
    }

    fun saveData(){
        viewModelScope.launch(Dispatchers.IO) {
            // 保存选择的引擎
            allEngines.forEach{
                DataStoreUtils.putData(it.selectKey, it.selected)
            }
            // 保存源语言、目标语言
            DataStoreUtils.putData(Consts.KEY_SOURCE_LANGUAGE,sourceLanguage.value!!.id)
            DataStoreUtils.putData(Consts.KEY_TARGET_LANGUAGE,targetLanguage.value!!.id)
            Log.d(TAG, "MainScreen: 保存选择数据完成")
        }
    }

    fun translate(){
        if(translateJob?.isActive==true)return
        if(actualTransText.isEmpty())return
        _resultList.clear()
        progress.value = 0

        translateJob = viewModelScope.launch {
            selectedEngines.forEach {
                if (support(it.supportLanguages)) {
                    val task = if (it is TranslationEngines){
                        it.createTask(actualTransText,sourceLanguage.value!!,targetLanguage.value!!)
                    }else{
                        val jsTask = it as JsTranslateTask
                        jsTask.sourceString = translateText.value!!
                        jsTask.sourceLanguage = sourceLanguage.value!!
                        jsTask.targetLanguage = targetLanguage.value!!
                        jsTask
                    }
                    try {
                        task.result.targetLanguage = targetLanguage.value!!

                        withContext(Dispatchers.IO) {
                            task.translate(translateMode.value!!)
                            Log.d(TAG, "translate : ${progress.value} ${task.result}")
                        }

                        updateTranslateResult(task.result)
                    } catch (e: TranslationException) {
                        with(task.result) {
                            setBasicResult(
                                "${FunnyApplication.resources.getString(R.string.error_result)}\n${e.message}"
                            )
                            updateTranslateResult(this)
                        }
                    } catch (e : Exception){
                        with(task.result) {
                            setBasicResult(FunnyApplication.resources.getString(R.string.error_result))
                            updateTranslateResult(this)
                        }
                    }
                }
            }
        }
    }

    private fun updateTranslateResult(result: TranslationResult){
        progress.value = progress.value!! + 100/totalProgress
        // Log.d(TAG, "updateTranslateResult: ${progress.value}")
        _resultList.add(result)

        resultList.value = _resultList
    }

    private fun support(supportLanguages : List<Language>) =
        supportLanguages.contains(sourceLanguage.value) && supportLanguages.contains(targetLanguage.value)

    companion object{
        const val TAG = "MainVM"
    }
}