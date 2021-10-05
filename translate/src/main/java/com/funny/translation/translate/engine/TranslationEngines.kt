package com.funny.translation.translate.engine

import com.funny.translation.trans.Language
import com.funny.translation.trans.TranslationEngine
import com.funny.translation.trans.allLanguages
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.R

private fun stringResource(id : Int) = FunnyApplication.resources.getString(id)

sealed class TranslationEngines : TranslationEngine{
    override var selected: Boolean = false
    override val supportLanguages: List<Language>
        get() = languageMapping.map { it.key }

    object BaiduNormal : TranslationEngines() {
        override val name: String
            get() = stringResource(R.string.engine_baidu)

        override val languageMapping: Map<Language, String>
            get() = mapOf(
                Language.AUTO to "auto",
                Language.CHINESE to "zh",
                Language.ENGLISH to "en",
                Language.JAPANESE to "jp",
                Language.KOREAN to "kor",
                Language.FRENCH to "fra",
                Language.RUSSIAN to "ru",
                Language.GERMANY to "de",
                Language.WENYANWEN to "wyw",
                Language.THAI to "th"
            )
    }

    object Jinshan : TranslationEngines() {
        override val name: String
            get() = stringResource(R.string.engine_jinshan)

        override val languageMapping: Map<Language, String>
            get() = mapOf(
                Language.AUTO to "auto",
                Language.CHINESE to "zh",
                Language.ENGLISH to "en-US",
                Language.JAPANESE to "ja",
                Language.KOREAN to "ko",
                Language.FRENCH to "fr",
                Language.RUSSIAN to "ru",
                Language.GERMANY to "de",
                Language.THAI to "th"
            )
    }

    object GoogleNormal : TranslationEngines() {
        override val languageMapping: Map<Language, String>
            get() = mapOf(
                Language.AUTO to "auto",
                Language.CHINESE to "zh-CN",
                Language.ENGLISH to "en",
                Language.JAPANESE to "ja",
                Language.KOREAN to "ko",
                Language.FRENCH to "fr",
                Language.RUSSIAN to "ru",
                Language.GERMANY to "de",
                Language.THAI to "th"
            )

        override val name: String
            get() = stringResource(R.string.engine_google)
    }

    object BiggerText : TranslationEngines(){
        override val name: String
            get() = stringResource(R.string.engine_bigger_text)

        override val languageMapping: Map<Language, String>
            get() = mapOf()

        override val supportLanguages: List<Language>
            get() = arrayListOf(Language.CHINESE, Language.ENGLISH, Language.AUTO)
    }

    object Bv2Av : TranslationEngines(){
        override val name: String
            get() = stringResource(R.string.engine_bv2av)

        override val languageMapping: Map<Language, String>
            get() = mapOf()

        override val supportLanguages: List<Language>
            get() = allLanguages
    }

    object EachText : TranslationEngines(){
        override val languageMapping: Map<Language, String>
            get() = mapOf()

        override val name: String
            get() = FunnyApplication.resources.getString(R.string.engine_each_text)

        override val supportLanguages: List<Language>
            get() = arrayListOf(Language.CHINESE, Language.ENGLISH, Language.AUTO)
    }

    object Youdao : TranslationEngines(){
        override val name: String
            get() = FunnyApplication.resources.getString(R.string.engine_youdao_normal)

        override val languageMapping: Map<Language, String>
            get() = mapOf(
                Language.AUTO to "auto",
                Language.CHINESE to "zh",
                Language.ENGLISH to "en-US",
                Language.JAPANESE to "ja",
                Language.KOREAN to "ko",
                Language.FRENCH to "fr",
                Language.RUSSIAN to "ru",
                Language.GERMANY to "de",
                Language.THAI to "th"
            )
    }

}