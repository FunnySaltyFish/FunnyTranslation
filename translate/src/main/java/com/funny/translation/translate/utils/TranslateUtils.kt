package com.funny.translation.translate.utils

import com.funny.translation.js.core.JsTranslateTask
import com.funny.translation.translate.Language
import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.engine.TranslationEngines

object TranslateUtils {
    fun createTask(translationEngine: TranslationEngine, actualTransText:String, sourceLanguage: Language, targetLanguage: Language) =
        if (translationEngine is TranslationEngines) {
            translationEngine.createTask(actualTransText, sourceLanguage, targetLanguage)
        } else {
            val jsTask = translationEngine as JsTranslateTask
            jsTask.sourceString = actualTransText
            jsTask.sourceLanguage = sourceLanguage
            jsTask.targetLanguage = targetLanguage
            jsTask
        }
}