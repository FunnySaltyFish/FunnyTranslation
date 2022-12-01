package com.funny.translation.translate.engine

import com.funny.translation.translate.*
import com.funny.translation.translate.task.ImageTranslationBaidu
import java.util.*
import kotlin.reflect.KClass

private fun stringResource(id : Int) = FunnyApplication.resources.getString(id)

interface ImageTranslationEngine: TranslationEngine {
    override val taskClass: KClass<out ImageTranslationTask>
    fun getPoint(): Float
}

sealed class ImageTranslationEngines: ImageTranslationEngine {
    override var selected: Boolean = false
    override val supportLanguages: List<Language>
        get() = languageMapping.map { it.key }

    object Baidu: ImageTranslationEngines() {
        override val name: String = stringResource(R.string.engine_baidu)
        override val supportLanguages: List<Language> = TextTranslationEngines.BaiduNormal.supportLanguages
        override val languageMapping: Map<Language, String> = TextTranslationEngines.BaiduNormal.languageMapping

        override val taskClass: KClass<out ImageTranslationTask>
            get() = TODO("Not yet implemented")

        override fun getPoint() = 1.0f
    }

    object Tencent: ImageTranslationEngines() {
        override val name: String = stringResource(R.string.engine_tencent)
        override val supportLanguages: List<Language> = allLanguages
        override val languageMapping: Map<Language, String> = EnumMap(Language::class.java)

        override val taskClass: KClass<out ImageTranslationTask> = ImageTranslationBaidu::class

        override fun getPoint() = 1.0f
    }

    
}
