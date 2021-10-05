package com.funny.translation.translate.engine

import com.funny.translation.trans.CoreTranslationTask
import com.funny.translation.trans.Language
import com.funny.translation.trans.TranslationEngine
import com.funny.translation.trans.allLanguages
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.R
import com.funny.translation.translate.task.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

private fun stringResource(id : Int) = FunnyApplication.resources.getString(id)

sealed class TranslationEngines : TranslationEngine{
    override var selected: Boolean = false
    override val supportLanguages: List<Language>
        get() = languageMapping.map { it.key }

    fun createTask(
        sourceString: String = "",
        sourceLanguage: Language = Language.AUTO,
        targetLanguage: Language = Language.ENGLISH
    ) : CoreTranslationTask {
        val instance = taskClass.createInstance()
        instance.sourceString = sourceString
        instance.sourceLanguage = sourceLanguage
        instance.targetLanguage = targetLanguage
        return instance
    }

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

        override val taskClass: KClass<out CoreTranslationTask>
            get() = TranslationBaiduNormal::class
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

        override val taskClass: KClass<out CoreTranslationTask>
            get() = TranslationJinshanEasy::class
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

        override val taskClass: KClass<out CoreTranslationTask>
            get() = TranslationGoogleNormal::class
    }

    object BiggerText : TranslationEngines(){
        override val name: String
            get() = stringResource(R.string.engine_bigger_text)

        override val languageMapping: Map<Language, String>
            get() = mapOf()

        override val supportLanguages: List<Language>
            get() = arrayListOf(Language.CHINESE, Language.ENGLISH, Language.AUTO)

        override val taskClass: KClass<out CoreTranslationTask>
            get() = TranslationBiggerText::class
    }

    object Bv2Av : TranslationEngines(){
        override val name: String
            get() = stringResource(R.string.engine_bv2av)

        override val languageMapping: Map<Language, String>
            get() = mapOf()

        override val supportLanguages: List<Language>
            get() = allLanguages

        override val taskClass: KClass<out CoreTranslationTask>
            get() = TranslationBV2AV::class
    }

    object EachText : TranslationEngines(){
        override val languageMapping: Map<Language, String>
            get() = mapOf()

        override val name: String
            get() = FunnyApplication.resources.getString(R.string.engine_each_text)

        override val supportLanguages: List<Language>
            get() = arrayListOf(Language.CHINESE, Language.ENGLISH, Language.AUTO)

        override val taskClass: KClass<out CoreTranslationTask>
            get() = TranslationEachText::class
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

        override val taskClass: KClass<out CoreTranslationTask>
            get() = TranslationYouDaoNormal::class
    }

}