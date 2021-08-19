package com.funny.translation.translation

import com.funny.translation.trans.CoreTranslationTask

interface OnTranslateListener {
    fun finishOne(task: CoreTranslationTask, e : Exception?)
    fun finishAll()
}