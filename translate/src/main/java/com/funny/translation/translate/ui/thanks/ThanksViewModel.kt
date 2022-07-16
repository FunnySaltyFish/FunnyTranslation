package com.funny.translation.translate.ui.thanks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.funny.translation.translate.network.TransNetwork
import com.funny.translation.translate.network.service.SponsorPagingSource

class ThanksViewModel : ViewModel() {
    val sponsorService = TransNetwork.sponsorService

    val sponsors = Pager(PagingConfig(pageSize = 10)) {
        SponsorPagingSource(sponsorService)
    }.flow.cachedIn(viewModelScope)

    companion object {
        private const val TAG = "ThanksVM"
    }
}