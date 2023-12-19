package com.funny.translation.translate.task

import com.funny.translation.translate.*


abstract class BasicTextTranslationTask() :
    CoreTextTranslationTask(){

    @Throws(TranslationException::class)
    override suspend fun translate() {
        val url = madeURL()
        result.engineName = name
        result.sourceString = sourceString
        try {
            if (sourceLanguage == targetLanguage) { //如果目标语言和源语言相同，跳过翻译
                result.setBasicResult(sourceString)
            } else {
                val basicText = getBasicText(url)
                getFormattedResult(basicText)
            }
        } catch (e: TranslationException) {
            e.printStackTrace()
            throw e
        }
    }

}