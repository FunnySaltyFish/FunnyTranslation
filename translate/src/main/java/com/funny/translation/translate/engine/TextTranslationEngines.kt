package com.funny.translation.translate.engine

import com.funny.translation.translate.*
import com.funny.translation.translate.task.*
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

private fun stringResource(id : Int) = FunnyApplication.resources.getString(id)

abstract class TextTranslationEngine: TranslationEngine {
    abstract override val taskClass: KClass<out CoreTextTranslationTask>
}

sealed class TextTranslationEngines : TextTranslationEngine() {
    override var selected: Boolean = false
    override val supportLanguages: List<Language>
        get() = languageMapping.map { it.key }

    fun createTask(
        sourceString: String = "",
        sourceLanguage: Language = Language.AUTO,
        targetLanguage: Language = Language.ENGLISH
    ) : CoreTextTranslationTask {
        val instance = taskClass.createInstance()
        instance.sourceString = sourceString
        instance.sourceLanguage = sourceLanguage
        instance.targetLanguage = targetLanguage
        return instance
    }

    object BaiduNormal : TextTranslationEngines() {
        override val name: String = stringResource(R.string.engine_baidu)

        override val languageMapping: HashMap<Language, String> = hashMapOf(
                Language.AUTO to "auto",
                Language.CHINESE to "zh",
                Language.ENGLISH to "en",
                Language.JAPANESE to "jp",
                Language.KOREAN to "kor",
                Language.FRENCH to "fra",
                Language.RUSSIAN to "ru",
                Language.GERMANY to "de",
                Language.WENYANWEN to "wyw",
                Language.THAI to "th",
                Language.PORTUGUESE to "pt",
                Language.VIETNAMESE to "vie",
                Language.ITALIAN to "it",
                Language.CHINESE_YUE to "yue",
                Language.SPANISH to "spa"
            )

        override val taskClass: KClass<out CoreTextTranslationTask> = TextTranslationBaiduNormal::class
    }

    object Jinshan : TextTranslationEngines() {
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
                Language.THAI to "th",
                Language.PORTUGUESE to "pt",
                Language.VIETNAMESE to "vi",
                Language.ITALIAN to "it"
            )

        override val taskClass: KClass<out CoreTextTranslationTask> = TextTranslationJinshanEasy::class

        override val supportLanguages: List<Language>
            get() = listOf(Language.AUTO, Language.CHINESE, Language.ENGLISH)
    }

    object GoogleNormal : TextTranslationEngines() {
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
                Language.THAI to "th",
                Language.PORTUGUESE to "pt",
                Language.VIETNAMESE to "vi",
                Language.ITALIAN to "it"
            )

        override val name: String
            get() = stringResource(R.string.engine_google)

        override val taskClass: KClass<out CoreTextTranslationTask>
            get() = TextTranslationGoogleNormal::class
    }

    object Youdao : TextTranslationEngines(){
        override val name: String
            get() = FunnyApplication.resources.getString(R.string.engine_youdao_normal)

        override val languageMapping: Map<Language, String>
            get() = mapOf(
                Language.AUTO to "auto",
                Language.CHINESE to "zh",
                Language.ENGLISH to "en",
                Language.JAPANESE to "ja",
                Language.KOREAN to "ko",
                Language.FRENCH to "fr",
                Language.RUSSIAN to "ru",
                Language.GERMANY to "de",
                Language.THAI to "th",
                Language.PORTUGUESE to "pt",
                Language.VIETNAMESE to "vi",
                Language.ITALIAN to "it",
                Language.SPANISH to "es"
            )

        override val taskClass: KClass<out CoreTextTranslationTask>
            get() = TextTranslationYouDaoNormal::class
    }

    object Tencent : TextTranslationEngines(){
        override val name: String
            get() = FunnyApplication.resources.getString(R.string.engine_tencent)

        /**
         *  auto：自动识别（识别为一种语言）
            zh：简体中文
            zh-TW：繁体中文
            en：英语
            ja：日语
            ko：韩语
            fr：法语
            es：西班牙语
            it：意大利语
            de：德语
            tr：土耳其语
            ru：俄语
            pt：葡萄牙语
            vi：越南语
            id：印尼语
            th：泰语
            ms：马来西亚语
            ar：阿拉伯语
            hi：印地语
         */
        override val languageMapping: Map<Language, String>
            get() = mapOf(
                Language.AUTO to "auto",
                Language.CHINESE to "zh",
                Language.ENGLISH to "en",
                Language.JAPANESE to "ja",
                Language.KOREAN to "ko",
                Language.FRENCH to "fr",
                Language.RUSSIAN to "ru",
                Language.GERMANY to "de",
                Language.THAI to "th",
                Language.PORTUGUESE to "pt",
                Language.VIETNAMESE to "vi",
                Language.ITALIAN to "it",
                Language.SPANISH to "es",
            )

        override val taskClass: KClass<out CoreTextTranslationTask>
            get() = TextTranslationTencent::class
    }

    object BiggerText : TextTranslationEngines(){
        override val name: String
            get() = stringResource(R.string.engine_bigger_text)

        override val languageMapping: Map<Language, String>
            get() = mapOf()

        override val supportLanguages: List<Language>
            get() = arrayListOf(Language.CHINESE, Language.ENGLISH, Language.AUTO)

        override val taskClass: KClass<out CoreTextTranslationTask>
            get() = TextTranslationBiggerText::class
    }

    object Bv2Av : TextTranslationEngines(){
        override val name: String
            get() = stringResource(R.string.engine_bv2av)

        override val languageMapping: Map<Language, String>
            get() = mapOf()

        override val supportLanguages: List<Language>
            get() = allLanguages

        override val taskClass: KClass<out CoreTextTranslationTask>
            get() = TextTranslationBV2AV::class
    }

    object EachText : TextTranslationEngines() {
        override val languageMapping: Map<Language, String>
            get() = mapOf()

        override val name: String
            get() = FunnyApplication.resources.getString(R.string.engine_each_text)

        override val supportLanguages: List<Language>
            get() = arrayListOf(Language.CHINESE, Language.ENGLISH, Language.AUTO)

        override val taskClass: KClass<out CoreTextTranslationTask>
            get() = TextTranslationEachText::class
    }

}