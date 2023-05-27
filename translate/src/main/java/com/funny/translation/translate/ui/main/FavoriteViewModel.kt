package com.funny.translation.translate.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.funny.translation.translate.database.TransFavoriteBean
import com.funny.translation.translate.database.appDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoriteViewModel: ViewModel() {
    val transFavorites by lazy {
        Pager(PagingConfig(pageSize = 10)) {
            appDB.transFavoriteDao.queryAllPaging()
        }.flow.cachedIn(viewModelScope)
    }

    fun deleteTransFavorite(bean: TransFavoriteBean){
        viewModelScope.launch(Dispatchers.IO) {
            appDB.transFavoriteDao.deleteTransFavorite(bean)
        }
    }
}