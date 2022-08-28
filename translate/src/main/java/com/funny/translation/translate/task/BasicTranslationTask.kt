package com.funny.translation.translate.task

import com.funny.translation.trans.*
import com.funny.translation.Consts


abstract class BasicTranslationTask() :
    CoreTranslationTask(){

    @Throws(TranslationException::class)
    override fun translate(mode: Int) {
        val url = madeURL()
        var processedString: String? = null
        result.engineName = name

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
        }finally {
            this.result.let {
                reFormatBasicText(it, mode) //还原处理过的basicText
                it.sourceString = sourceString
            }
        }
    }

    private fun reFormatBasicText(translationResult: TranslationResult, mode: Int) {
        when (mode) {
            Consts.MODE_EACH_LINE -> {
                val sb = StringBuilder()
                val sourceArr = sourceString.split("\n").toTypedArray()
                val targetArr: List<String> = translationResult.basicResult.trans.split("\n")
                val length = Math.min(sourceArr.size, targetArr.size)
                var i = 0
                while (i < length) {
                    sb.append(sourceArr[i])
                    sb.append("\n")
                    sb.append(targetArr[i])
                    sb.append("\n")
                    i++
                }
                sb.deleteCharAt(sb.length - 1)
                translationResult.setBasicResult(sb.toString())
            }
            else -> {
            }
        }
    }

}