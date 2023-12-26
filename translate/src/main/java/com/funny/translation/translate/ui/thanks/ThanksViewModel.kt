package com.funny.translation.translate.ui.thanks

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.funny.translation.translate.network.TransNetwork
import com.funny.translation.translate.network.service.SponsorPagingSource

class ThanksViewModel : ViewModel() {
    private val sponsorService = TransNetwork.sponsorService

    private val sponsorSort = mutableStateOf("")

    val sponsors = Pager(PagingConfig(pageSize = 10)){
        SponsorPagingSource(sponsorService, sponsorSort.value)
    }.flow.cachedIn(viewModelScope)

    fun updateSort(sortType: SponsorSortType, sortOrder: Int){
        if (sortType == SponsorSortType.Date) sponsorSort.value = "[(\"${sortType.value}\", $sortOrder)]"
        else sponsorSort.value = "[(\"${sortType.value}\", $sortOrder), (\"date\", 1)]"
    }

    companion object {
        private const val TAG = "ThanksVM"
    }
}