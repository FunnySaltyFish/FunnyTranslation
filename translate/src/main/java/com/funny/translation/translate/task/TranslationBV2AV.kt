package com.funny.translation.translate.task

import com.funny.translation.trans.Language
import com.funny.translation.trans.TranslationException
import com.funny.translation.trans.allLanguages
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.R
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.utils.FunnyBvToAv
import com.funny.translation.translate.utils.StringUtil

class TranslationBV2AV(sourceString: String?, sourceLanguage: Language, targetLanguage: Language) :
    BasicTranslationTask(
        sourceString!!, sourceLanguage, targetLanguage
    ) {

    override val languageMapping: Map<Language, String>
        get() = TODO("Not yet implemented")

    override val supportLanguages: List<Language>
        get() = allLanguages

    override val name: String
        get() = FunnyApplication.resources.getString(R.string.engine_bv2av)

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
                    result = FunnyBvToAv.dec(bv)
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