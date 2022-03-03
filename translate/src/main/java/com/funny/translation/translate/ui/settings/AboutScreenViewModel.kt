package com.funny.translation.translate.ui.settings

import androidx.lifecycle.ViewModel
import com.funny.translation.helper.readAssets
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.bean.OpenSourceLibraryInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AboutScreenViewModel : ViewModel() {
    lateinit var openSourceLibraryList: List<OpenSourceLibraryInfo>
    suspend fun loadOpenSourceLibInfo(): List<OpenSourceLibraryInfo> =
        if (this@AboutScreenViewModel::openSourceLibraryList.isInitialized) openSourceLibraryList
        else
            withContext(Dispatchers.IO) {
                val json = FunnyApplication.ctx.readAssets("open_source_libraries.json")
                val type = object : TypeToken<List<OpenSourceLibraryInfo>>() {}.type
                openSourceLibraryList = Gson().fromJson(json, type)
                openSourceLibraryList
            }
}