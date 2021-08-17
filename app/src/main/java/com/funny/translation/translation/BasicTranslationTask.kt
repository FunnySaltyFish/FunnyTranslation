package com.funny.translation.translation

import com.funny.translation.bean.Consts
import com.funny.translation.trans.CoreTranslationTask
import com.funny.translation.trans.TranslationException
import com.funny.translation.trans.TranslationResult
import com.funny.translation.utils.StringUtil
import java.lang.StringBuilder


abstract class BasicTranslationTask(sourceString: String,sourceLanguage : Short, targetLanguage : Short) :
    CoreTranslationTask(sourceString, sourceLanguage, targetLanguage) {

    @Throws(TranslationException::class)
    override fun translate(mode: Short) {
        val url = madeURL()
        var processedString: String? = null
        val engineKind = engineKind
        result = TranslationResult(engineKind)
        try {
            if (mode === Consts.MODE_EACH_TEXT && sourceLanguage !== Consts.LANGUAGE_CHINESE) {
                throw TranslationException(Consts.ERROR_ONLY_CHINESE_SUPPORT)
            }
            if (engineKind == Consts.ENGINE_BIGGER_TEXT && !StringUtil.isValidContent(StringUtil.extraChinese(sourceString))
            ) {
                throw TranslationException(Consts.ERROR_ONLY_CHINESE_SUPPORT)
            }
            processedString = getProcessedString(sourceString, mode)
            //Log.i(TAG,"获取到的processedString是"+processedString);
            result.sourceString = processedString
            if (sourceLanguage === targetLanguage) { //如果目标语言和源语言相同，跳过翻译
                result.setBasicResult(sourceString)
            } else {
                val basicText = getBasicText(url)
                result = getFormattedResult(basicText)
            }
        } catch (e: TranslationException) {
            e.printStackTrace()
            throw e
        }finally {
            result.let {
                reFormatBasicText(it, mode) //还原处理过的basicText
                it.sourceString = sourceString
            }
        }
    }

    @Throws(TranslationException::class)
    private fun getProcessedString(str: String, mode: Short): String {
        return when (mode) {
            Consts.MODE_EACH_TEXT -> {
                val chinese = StringUtil.extraChinese(str)
                if (!StringUtil.isValidContent(chinese)) {
                    throw TranslationException(Consts.ERROR_ONLY_CHINESE_SUPPORT)
                }
                StringUtil.insertJuhao(chinese)
            }
            else -> str
        }
    }

    private fun reFormatBasicText(translationResult: TranslationResult, mode: Short) {
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
            Consts.MODE_EACH_TEXT -> {
                val basicResult: String = translationResult.basicResult.trans
                //Log.i(TAG,"获取到的basicResult :"+basicResult);
                var reFormattedString = basicResult.replace("。".toRegex(), "")
                reFormattedString = reFormattedString.replace(",".toRegex(), " ")
                translationResult.setBasicResult(reFormattedString)
            }
            else -> {
            }
        }
    }

}