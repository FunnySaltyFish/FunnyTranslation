package com.funny.translation.translate.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.translation.helper.JsonX
import com.funny.translation.helper.lazyPromise
import com.funny.translation.helper.readAssets
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.bean.OpenSourceLibraryInfo
import com.funny.translation.translate.utils.SortResultUtils
import com.funny.translation.translate.utils.TranslationEngineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class SettingsScreenViewModel : ViewModel() {
    companion object {
        private const val TAG = "SettingsScreenVM"
    }

    private val openSourceLibraryList by lazyPromise<List<OpenSourceLibraryInfo>>(viewModelScope){
        withContext(Dispatchers.IO) {
            val json = FunnyApplication.ctx.readAssets("open_source_libraries.json")
            JsonX.fromJson(json)
        }
    }

    suspend fun loadOpenSourceLibInfo(): List<OpenSourceLibraryInfo> = openSourceLibraryList.await()
}