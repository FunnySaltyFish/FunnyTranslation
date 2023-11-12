package com.funny.translation.translate.ui.long_text

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.translation.translate.database.LongTextTransTaskMini
import com.funny.translation.translate.database.appDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LongTextTransListViewModel: ViewModel() {
    private val dao = appDB.longTextTransDao

    val taskList = appDB.longTextTransDao.getAllMini()

    fun deleteTask(task: LongTextTransTaskMini) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteById(task.id)
        }
    }

    fun updateRemark(longTextTransTask: LongTextTransTaskMini, newRemark: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateRemark(longTextTransTask.id, newRemark)
        }
    }
}