package com.funny.translation.translate.ui.thanks

import androidx.lifecycle.ViewModel
import com.funny.translation.translate.network.TransNetwork

class ThanksViewModel : ViewModel() {
    val sponsorService = TransNetwork.sponsorService

    companion object {
        private const val TAG = "ThanksVM"
    }
}