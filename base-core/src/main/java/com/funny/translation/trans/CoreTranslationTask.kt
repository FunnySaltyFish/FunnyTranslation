package com.funny.translation.trans

abstract class CoreTranslationTask(
    var sourceString: String,
    var sourceLanguage: Short,
    var targetLanguage: Short
) {
    lateinit var result: TranslationResult
    @Throws(TranslationException::class)
    abstract fun getBasicText(url: String): String
    @Throws(TranslationException::class)
    abstract fun getFormattedResult(basicText: String): TranslationResult
    abstract fun madeURL(): String
    abstract val isOffline: Boolean
    abstract val engineKind: Short
    @Throws(TranslationException::class)
    abstract fun translate(mode: Short)

    companion object {
        private const val TAG = "BasicTranslationTask"
    }
}