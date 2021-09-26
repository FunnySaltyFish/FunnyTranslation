package com.funny.translation.trans

interface TranslationEngine {
    val name : String
    val id : Int
    val supportLanguages: List<Language>
}
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
//class EngineBaidu() : TranslationEngine()
//class EngineJinshan() : TranslationEngine()
//class EngineGoogle() : TranslationEngine()
