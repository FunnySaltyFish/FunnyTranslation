package com.funny.translation.translate.task

import com.funny.translation.trans.Language
import com.funny.translation.trans.TranslationEngine
import com.funny.translation.trans.TranslationException
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.engine.TranslationEngines
import com.funny.translation.translate.utils.FunnyBvToAv
import com.funny.translation.translate.utils.StringUtil

class TranslationBV2AV() :
    BasicTranslationTask(), TranslationEngine by TranslationEngines.Bv2Av{

    @Throws(TranslationException::class)
    override fun getBasicText(url: String): String {
        var result = ""
        val inputText = sourceString
        if (inputText.isEmpty()) {
            return result
        }
        if (StringUtil.isNumber(inputText)) {
            result = FunnyBvToAv.enc(inputText.toLong())
        } else {
            var av: Long
            if (StringUtil.findAv(inputText).also { av = it } > 0) {
                result = FunnyBvToAv.enc(av)
            } else {
                var bv: String?
                if (StringUtil.findBv(inputText).also { bv = it } != "") {
                    result = FunnyBvToAv.dec(bv!!)
                }
            }
        }
        return result
    }

    @Throws(TranslationException::class)
    override fun getFormattedResult(basicText: String) {
        if (basicText != "") {
            result.setBasicResult(basicText)
        } else {
            throw TranslationException(Consts.ERROR_NO_BV_OR_AV)
        }
    }

    override fun madeURL(): String {
        return ""
    }

    override val isOffline: Boolean
        get() = true
}