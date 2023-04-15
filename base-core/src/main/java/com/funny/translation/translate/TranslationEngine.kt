package com.funny.translation.translate

import kotlin.reflect.KClass

/**
 * 翻译引擎
 * @property name String 引擎名称，全应用唯一
 * @property supportLanguages List<Language> 该引擎支持的语言
 * @property languageMapping Map<Language, String> 各语言对应的code，如"zh-CN"等
 * @property selected Boolean 记录当前引擎是否被选择
 * @property taskClass KClass<out CoreTranslationTask> 引擎运行时生成的任务，通过反射动态生成
 */
interface TranslationEngine {
    val name : String
    val supportLanguages: List<Language>
    val languageMapping : Map<Language , String>
    var selected : Boolean

    val taskClass : KClass<out CoreTranslationTask>
}