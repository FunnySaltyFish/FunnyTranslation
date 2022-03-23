package com.funny.translation.trans

import kotlin.reflect.KClass

interface TranslationEngine {
    val name : String
    val supportLanguages: List<Language>
    val languageMapping : Map<Language , String>
    var selected : Boolean

    val taskClass : KClass<out CoreTranslationTask>
}
val TranslationEngine.selectKey
    get() = this.name + "_SELECTED"


//sealed class TranslationEngine(
//        val name: String = "",
//        val id: Int = 0,
//        val supportLanguages: Array<Language> = getAllLanguages()
//) {
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as TranslationEngine
//
//        if (id != other.id) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int {
//        return id
//    }
//}
//
