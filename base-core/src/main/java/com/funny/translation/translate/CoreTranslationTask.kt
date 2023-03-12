package com.funny.translation.translate

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

abstract class CoreTranslationTask(
    var sourceLanguage: Language = Language.AUTO,
    var targetLanguage: Language = Language.ENGLISH
) : TranslationEngine

abstract class CoreTextTranslationTask(
    var sourceString: String = "",
) : CoreTranslationTask() {
    val result by lazy { TranslationResult() }
    var mutex: Mutex? = null

    @Throws(TranslationException::class)
    abstract fun getBasicText(url: String): String
    @Throws(TranslationException::class)
    abstract fun getFormattedResult(basicText: String)
    abstract fun madeURL(): String
    abstract val isOffline: Boolean

    @Throws(TranslationException::class)
    abstract suspend fun translate()

    /**
     * 检查是否有 mutex，是的话加锁，否则直接执行
     * @param mutex Mutex?
     * @param block Function0<Unit>
     */
    suspend inline fun doWithMutex(block: () -> Unit) = if (mutex == null) {
        block()
    } else {
        mutex?.withLock {
            block()
        }
    }

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