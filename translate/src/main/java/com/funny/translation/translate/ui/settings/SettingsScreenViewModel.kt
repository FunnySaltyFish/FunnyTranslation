package com.funny.translation.translate.ui.settings

import androidx.lifecycle.ViewModel
import com.funny.translation.helper.JsonX
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

    private lateinit var openSourceLibraryList: List<OpenSourceLibraryInfo>
    suspend fun loadOpenSourceLibInfo(): List<OpenSourceLibraryInfo> =
        if (this@SettingsScreenViewModel::openSourceLibraryList.isInitialized) openSourceLibraryList
        else
            withContext(Dispatchers.IO) {
                val json = FunnyApplication.ctx.readAssets("open_source_libraries.json")
                openSourceLibraryList = JsonX.fromJson(json)
                openSourceLibraryList
            }

    val localEngineNamesState : List<TranslationEngineName> by lazy {
        runBlocking {
            return@runBlocking SortResultUtils.getLocalEngineNames()
        }
    }

}