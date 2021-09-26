package com.funny.translation.translate.task

import com.funny.translation.trans.CoreTranslationTask

interface OnTranslateListener {
    fun finishOne(task: CoreTranslationTask, e : Exception?)
    fun finishAll()
}