package com.funny.translation.translate.utils

import com.funny.translation.js.core.JsTranslateTaskText
import com.funny.translation.translate.Language
import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.engine.TextTranslationEngines

object TranslateUtils {
    fun createTask(translationEngine: TranslationEngine, actualTransText:String, sourceLanguage: Language, targetLanguage: Language) =
        if (translationEngine is TextTranslationEngines) {
            translationEngine.createTask(actualTransText, sourceLanguage, targetLanguage)
        } else {
            val jsTask = translationEngine as JsTranslateTaskText
            jsTask.sourceString = actualTransText
            jsTask.sourceLanguage = sourceLanguage
            jsTask.targetLanguage = targetLanguage
            jsTask
        }
}