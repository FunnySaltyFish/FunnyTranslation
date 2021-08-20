package com.funny.translation.trans

abstract class CoreTranslationTask(
    var sourceString: String,
    var sourceLanguage: Short,
    var targetLanguage: Short
) {
    val result = TranslationResult()
    @Throws(TranslationException::class)
    abstract fun getBasicText(url: String): String
    @Throws(TranslationException::class)
    abstract fun getFormattedResult(basicText: String): Unit
    abstract fun madeURL(): String
    abstract val isOffline: Boolean
    abstract val engineKind: Short
    @Throws(TranslationException::class)
    abstract fun translate(mode: Short = 0)

    companion object {
        private const val TAG = "BasicTranslationTask"
    }
}