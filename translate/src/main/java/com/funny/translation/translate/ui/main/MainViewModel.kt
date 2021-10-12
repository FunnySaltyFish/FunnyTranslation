package com.funny.translation.translate.ui.main

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.translation.js.JsEngine
import com.funny.translation.js.core.JsTranslateTask
import com.funny.translation.trans.Language
import com.funny.translation.trans.TranslationEngine
import com.funny.translation.trans.TranslationException
import com.funny.translation.trans.TranslationResult
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.R
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

    val sourceLanguage : MutableLiveData<Language> = MutableLiveData(Language.ENGLISH)
    val targetLanguage : MutableLiveData<Language> = MutableLiveData(Language.CHINESE)
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

    val jsEngines : Flow<List<JsTranslateTask>> = appDB.jsDao.getAllJs().map { list ->
        list.map {
            JsTranslateTask(jsEngine = JsEngine(jsBean = it))
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
        TranslationEngines.BaiduNormal.selected = true
        TranslationEngines.Youdao.selected = true
    }

    fun translate(){
        if(translateJob?.isActive==true)return
        if(translateText.value!!.isEmpty())return
        _resultList.clear()
        progress.value = 0

        translateJob = viewModelScope.launch {
            selectedEngines.forEach {
                if (support(it.supportLanguages)) {
                    val task = if (it is TranslationEngines){
                        it.createTask(translateText.value!!,sourceLanguage.value!!,targetLanguage.value!!)
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
        Log.d(TAG, "updateTranslateResult: ${progress.value}")
        _resultList.add(result)

        resultList.value = _resultList
    }

    private fun support(supportLanguages : List<Language>) =
        supportLanguages.contains(sourceLanguage.value) && supportLanguages.contains(targetLanguage.value)

    companion object{
        const val TAG = "MainVM"
    }
}