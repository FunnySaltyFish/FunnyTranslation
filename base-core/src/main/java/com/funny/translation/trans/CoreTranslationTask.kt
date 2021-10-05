package com.funny.translation.trans

abstract class CoreTranslationTask(
    var sourceString: String,
    var sourceLanguage: Language,
    var targetLanguage: Language
) : TranslationEngine {
    val result = TranslationResult()
//    abstract val languageMapping : Map<Language, String>
//    abstract val supportLanguages: List<Language>
//    abstract val name : String
    @Throws(TranslationException::class)
    abstract fun getBasicText(url: String): String
    @Throws(TranslationException::class)
    abstract fun getFormattedResult(basicText: String): Unit
    abstract fun madeURL(): String
    abstract val isOffline: Boolean

    @Throws(TranslationException::class)
    abstract fun translate(mode: Int = 0)

    companion object {
        private const val TAG = "BasicTranslationTask"
    }
}