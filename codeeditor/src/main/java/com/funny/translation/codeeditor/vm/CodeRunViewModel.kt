package com.funny.translation.codeeditor.vm

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.funny.translation.codeeditor.base.BaseViewModel

class CodeRunViewModel(application: Application) : BaseViewModel(application) {
    val output = MutableLiveData("")
}