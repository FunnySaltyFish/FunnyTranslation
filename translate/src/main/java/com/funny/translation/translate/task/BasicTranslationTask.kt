package com.funny.translation.translate.task

import com.funny.translation.trans.*
import com.funny.translation.translate.bean.Consts


abstract class BasicTranslationTask(
    sourceString: String,
    sourceLanguage: Language, targetLanguage: Language
) :
    CoreTranslationTask(sourceString, sourceLanguage, targetLanguage){

    @Throws(TranslationException::class)
    override fun translate(mode: Int) {
        val url = madeURL()
        var processedString: String? = null
        result.engineName = name

        try {
            //TODO("某个语言不支持")
//            if (engineName == Consts.ENGINE_BIGGER_TEXT && (StringUtil.extraChinese(sourceString))
//            ) {
//                throw TranslationException(Consts.ERROR_ONLY_CHINESE_SUPPORT)
//            }
            processedString = getProcessedString(sourceString, mode)
            //Log.i(TAG,"获取到的processedString是"+processedString);
            result.sourceString = processedString
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

    @Throws(TranslationException::class)
    private fun getProcessedString(str: String, mode: Int): String {
        return str
//        return when (mode) {
//            Consts.MODE_EACH_TEXT -> {
//                val chinese = StringUtil.extraChinese(str)
//                if (!StringUtil.isValidContent(chinese)) {
//                    throw TranslationException(Consts.ERROR_ONLY_CHINESE_SUPPORT)
//                }
//                StringUtil.insertJuhao(chinese)
//            }
//            else -> str
//        }
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