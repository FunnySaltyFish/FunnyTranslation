package com.funny.translation.translate.engine

import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.ImageTranslationTask
import com.funny.translation.translate.Language
import com.funny.translation.translate.R
import com.funny.translation.translate.TranslationEngine
import com.funny.translation.translate.task.ImageTranslationBaidu
import com.funny.translation.translate.task.ImageTranslationTencent
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

private fun stringResource(id : Int) = FunnyApplication.resources.getString(id)

interface ImageTranslationEngine: TranslationEngine {
    override val taskClass: KClass<out ImageTranslationTask>
    fun getPoint(): Float
    fun createTask(
        sourceImage: ByteArray,
        sourceLanguage: Language = Language.AUTO,
        targetLanguage: Language = Language.ENGLISH
    ) : ImageTranslationTask
}

sealed class ImageTranslationEngines: ImageTranslationEngine {
    override var selected: Boolean = false
    override val supportLanguages: List<Language>
        get() = languageMapping.map { it.key }

    override fun createTask(
        sourceImage: ByteArray,
        sourceLanguage: Language,
        targetLanguage: Language
    ) : ImageTranslationTask {
        val instance = taskClass.createInstance()
        instance.sourceImg = sourceImage
        instance.sourceLanguage = sourceLanguage
        instance.targetLanguage = targetLanguage
        return instance
    }

object Baidu: ImageTranslationEngines() {
        override val name: String = stringResource(R.string.engine_baidu)
        override val supportLanguages: List<Language> = TextTranslationEngines.BaiduNormal.supportLanguages
        override val languageMapping: Map<Language, String> = TextTranslationEngines.BaiduNormal.languageMapping

        override val taskClass: KClass<out ImageTranslationTask> = ImageTranslationBaidu::class

        override fun getPoint() = 1.0f
    }

    object Tencent: ImageTranslationEngines() {
        override val name: String = stringResource(R.string.engine_tencent)
        override val languageMapping: Map<Language, String> = hashMapOf(
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
        )
        override val supportLanguages: List<Language> = languageMapping.keys.toList()
        override val taskClass: KClass<out ImageTranslationTask> = ImageTranslationTencent::class

        override fun getPoint() = 1.0f
    }

    
}
