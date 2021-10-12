package com.funny.translation.translate

import androidx.lifecycle.ViewModel
import com.funny.translation.js.JsEngine
import com.funny.translation.js.core.JsTranslateTask
import com.funny.translation.translate.database.appDB
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ActivityViewModel : ViewModel() {
    var lastBackTime : Long = 0
}