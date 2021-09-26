package com.funny.translation.translate.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.translation.trans.*
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    val translateText = MutableLiveData("")

    val sourceLanguage : MutableLiveData<Language> = MutableLiveData(Language.CHINESE)
    val targetLanguage : MutableLiveData<Language> = MutableLiveData(Language.ENGLISH)
    val translateMode : MutableLiveData<Int> = MutableLiveData(0)

    val selectedEngines : MutableLiveData<ArrayList<CoreTranslationTask>> = MutableLiveData(arrayListOf())
    val resultList : ArrayList<TranslationResult> = arrayListOf()

    val progress : MutableLiveData<Int> = MutableLiveData(0)
    val totalProgress : Int
        get() = selectedEngines.value!!.map { support(it.supportLanguages)==true }.size

    lateinit var translateJob : Job

    fun translate(){
        if(translateJob.isActive){
            return
        }
        resultList.clear()
        translateJob = viewModelScope.launch {
            selectedEngines.value!!.forEach {
                if (support(it.supportLanguages)) {
                    try {
                        async {
                            it.translate(translateMode.value!!)
                        }.await()
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
        resultList.add(result)
    }

    private fun support(supportLanguages : List<Language>) =
        supportLanguages.contains(sourceLanguage.value) && supportLanguages.contains(targetLanguage.value)

}