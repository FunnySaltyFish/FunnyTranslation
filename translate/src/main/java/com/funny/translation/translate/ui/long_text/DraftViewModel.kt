package com.funny.translation.translate.ui.long_text

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.translation.translate.database.Draft
import com.funny.translation.translate.database.appDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DraftViewModel: ViewModel() {
    private val dao = appDB.draftDao
    val draftList = appDB.draftDao.getAll()

    fun deleteDraft(draft: Draft) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.delete(draft)
        }
    }
}