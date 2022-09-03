package com.funny.translation.translate

abstract class CoreTranslationTask(
    var sourceString: String = "",
    var sourceLanguage: Language = Language.AUTO,
    var targetLanguage: Language = Language.ENGLISH
) : TranslationEngine {
    val result = TranslationResult()
//    abstract val languageMapping : Map<Language, String>
//    abstract val supportLanguages: List<Language>
//    abstract val name : String
    @Throws(TranslationException::class)
    abstract fun getBasicText(url: String): String
    @Throws(TranslationException::class)
    abstract fun getFormattedResult(basicText: String)
    abstract fun madeURL(): String
    abstract val isOffline: Boolean

    @Throws(TranslationException::class)
    abstract fun translate(mode: Int = 0)

    override fun equals(other: Any?): Boolean {
        return (other is TranslationEngine && other.name == name)
    }

    override fun hashCode(): Int {
        return name.hashCode() + 1
    }

    override fun toString(): String = "Engine[$name]"

    companion object {
        private const val TAG = "BasicTranslationTask"
    }
}