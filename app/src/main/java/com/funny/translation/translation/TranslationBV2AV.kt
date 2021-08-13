package com.funny.translation.translation

import android.text.TextUtils
import com.funny.translation.bean.Consts
import com.funny.translation.trans.TranslationException
import com.funny.translation.trans.TranslationResult
import com.funny.translation.utils.FunnyBvToAv
import com.funny.translation.utils.StringUtil

class TranslationBV2AV(sourceString: String?, sourceLanguage: Short, targetLanguage: Short) :
    BasicTranslationTask(
        sourceString!!, sourceLanguage, targetLanguage
    ) {
    @Throws(TranslationException::class)
    override fun getBasicText(url: String): String {
        var result = ""
        val inputText = sourceString
        if (TextUtils.isEmpty(inputText)) {
            return result
        }
        if (StringUtil.isNumber(inputText)) {
            result = FunnyBvToAv.enc(inputText.toLong())
        } else {
            var av: Long = 0
            if (StringUtil.findAv(inputText).also { av = it } > 0) {
                result = FunnyBvToAv.enc(av)
            } else {
                var bv: String? = ""
                if (StringUtil.findBv(inputText).also { bv = it } != "") {
                    result = FunnyBvToAv.dec(bv)
                }
            }
        }
        return result
    }

    @Throws(TranslationException::class)
    override fun getFormattedResult(basicText: String): TranslationResult {
        val result = TranslationResult(engineKind)
        if (basicText != "") {
            result.setBasicResult(basicText)
        } else {
            throw TranslationException(Consts.ERROR_NO_BV_OR_AV)
        }
        return result
    }

    override fun madeURL(): String {
        return ""
    }

    override val isOffline: Boolean
        get() = true
    override val engineKind: Short
        get() = Consts.ENGINE_BV_TO_AV
}