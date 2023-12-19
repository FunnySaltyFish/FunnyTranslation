package com.funny.translation.translate.task

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import com.funny.compose.ai.bean.Model
import com.funny.translation.network.OkHttpUtils
import com.funny.translation.translate.CoreTranslationTask
import com.funny.translation.translate.Language
import com.funny.translation.translate.TranslationException
import com.funny.translation.translate.allLanguages
import com.funny.translation.translate.appCtx
import java.util.Locale
import kotlin.reflect.KClass


const val MODEL_NAME_PREFIX = "model_"

class ModelTranslationTask(val model: Model): ServerTextTranslationTask() {
    override val engineCodeName: String
        get() = MODEL_NAME_PREFIX + model.chatBotId

    override val name: String = model.name
    override val supportLanguages: List<Language> = allLanguages
    override val languageMapping: Map<Language, String> get() = englishNamesMapping

    override var selected: Boolean = false

    override val taskClass: KClass<out CoreTranslationTask>
        get() = this::class

    @Throws(TranslationException::class)
    override fun getBasicText(url: String): String {
        val from = languageMapping[sourceLanguage] ?: "(automatic)"
        val to = languageMapping[targetLanguage] ?: "Chinese"
        val params = hashMapOf(
            "source" to from,
            "target" to to,
            "text" to sourceString,
            "engine" to engineCodeName
        )
        return OkHttpUtils.get(url, params = params, timeout = intArrayOf(10, 300, 15))
    }

    companion object {
        private const val TAG = "ModelTranslationTask"
        val englishNamesMapping by lazy(LazyThreadSafetyMode.PUBLICATION) {
            Language.values().associateWith { lang ->
                lang.displayText
            }
        }
    }
}

private fun <T> withEnResource(block: (resources: Resources) -> T): T {
    var context: Context = appCtx
    val resources: Resources = context.resources
    val configuration: Configuration = resources.configuration
    val originalLocale: Locale = configuration.locale
    try {
        // 强制设置为英文
        val locale = Locale("en")
        Locale.setDefault(locale)
        configuration.setLocale(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context = context.createConfigurationContext(configuration)
        } else {
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }

        // 获取资源
        return block(context.resources)
    } finally {
        // 恢复原始的locale
        configuration.setLocale(originalLocale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context = context.createConfigurationContext(configuration)
        } else {
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
        Locale.setDefault(originalLocale)
    }

}