package com.funny.translation.translate.ui.settings

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.translation.helper.readAssets
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.bean.OpenSourceLibraryInfo
import com.funny.translation.translate.utils.SortResultUtils
import com.funny.translation.translate.utils.TranslationEngineName
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsScreenViewModel : ViewModel() {
    lateinit var openSourceLibraryList: List<OpenSourceLibraryInfo>
    suspend fun loadOpenSourceLibInfo(): List<OpenSourceLibraryInfo> =
        if (this@SettingsScreenViewModel::openSourceLibraryList.isInitialized) openSourceLibraryList
        else
            withContext(Dispatchers.IO) {
                val json = FunnyApplication.ctx.readAssets("open_source_libraries.json")
                val type = object : TypeToken<List<OpenSourceLibraryInfo>>() {}.type
                openSourceLibraryList = Gson().fromJson(json, type)
                openSourceLibraryList
            }

    var localEngineNamesState : MutableState<List<TranslationEngineName>> = mutableStateOf(listOf())

    init {
        viewModelScope.launch {
            localEngineNamesState.value = SortResultUtils.getLocalEngineNames()
        }
    }

}