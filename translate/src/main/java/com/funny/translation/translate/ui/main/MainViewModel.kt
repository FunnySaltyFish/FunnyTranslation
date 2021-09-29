package com.funny.translation.translate.ui.main

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.translation.trans.CoreTranslationTask
import com.funny.translation.trans.Language
import com.funny.translation.trans.TranslationException
import com.funny.translation.trans.TranslationResult
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.R
import com.funny.translation.translate.task.TranslationBaiduNormal
import com.funny.translation.translate.task.TranslationYouDaoNormal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
    val translateText = MutableLiveData("")

    val sourceLanguage : MutableLiveData<Language> = MutableLiveData(Language.ENGLISH)
    val targetLanguage : MutableLiveData<Language> = MutableLiveData(Language.CHINESE)
    val translateMode : MutableLiveData<Int> = MutableLiveData(0)

    val selectedEngines : MutableLiveData<ArrayList<CoreTranslationTask>> = MutableLiveData(arrayListOf(
        TranslationBaiduNormal(translateText.value!!,sourceLanguage.value!!,targetLanguage.value!!),
        TranslationYouDaoNormal(translateText.value!!,sourceLanguage.value!!,targetLanguage.value!!)
    ))

    val resultList : MutableLiveData<ArrayList<TranslationResult>> = MutableLiveData(arrayListOf())
    private val _resultList : ArrayList<TranslationResult> = arrayListOf()

    val progress : MutableLiveData<Int> = MutableLiveData(0)
    val totalProgress : Int
        get() = selectedEngines.value!!.map { support(it.supportLanguages) }.size

    var translateJob : Job? = null

    fun translate(){
        if(translateJob?.isActive==true)return
        _resultList.clear()
        progress.value = 0

        translateJob = viewModelScope.launch {
            selectedEngines.value!!.forEach {
                if (support(it.supportLanguages)) {
                    try {
                        it.sourceString = translateText.value!!
                        withContext(Dispatchers.IO) {
                            it.translate(translateMode.value!!)
                            Log.d(TAG, "translate : ${progress.value} ${it.result}")
                        }
                        updateTranslateResult(it.result)
                    } catch (e: TranslationException) {
                        with(it.result) {
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