package com.funny.translation.js.core

import android.util.Log
import com.funny.translation.debug.Debug
import com.funny.translation.js.JsEngine
import com.funny.translation.js.config.JsConfig.Companion.INVOCABLE
import com.funny.translation.js.config.JsConfig.Companion.SCRIPT_ENGINE
import com.funny.translation.js.extentions.messageWithDetail
import com.funny.translation.translate.*
import kotlinx.coroutines.sync.Mutex
import java.lang.Exception
import javax.script.ScriptException
import kotlin.math.absoluteValue
import kotlin.reflect.KClass

private const val TAG = "JsTranslateTask"

class JsTranslateTaskText(
    val jsEngine: JsEngine,
):
    CoreTextTranslationTask(){

    override val languageMapping: Map<Language, String>
        get() = mapOf()

    override var selected: Boolean = false

    override val supportLanguages: List<Language>
        get() = allLanguages

    override val name: String
        get() = jsEngine.jsBean.fileName

    override val taskClass: KClass<out CoreTextTranslationTask>
        get() = this::class

    override fun getBasicText(url: String): String {
        val obj = INVOCABLE.invokeMethod(jsEngine.funnyJS, "getBasicText", url)
        //Log.d(TAG, "getBasicText: ${obj is String}")
        return obj as String
    }

    override fun getFormattedResult(basicText: String) {
        INVOCABLE.invokeMethod(
            jsEngine.funnyJS,
            "getFormattedResult",
            basicText
        )
    }

    override fun madeURL(): String {
        val obj = INVOCABLE.invokeMethod(jsEngine.funnyJS, "madeURL")
        return obj as String
    }

    override val isOffline: Boolean
        get() =
            try {
                INVOCABLE.invokeMethod(jsEngine.funnyJS,"isOffline") as Boolean
            } catch (e: Exception) {
                true
            }

    override suspend fun translate() {
        fun String.emptyString() = this.ifEmpty { " [空字符串]" }
        doWithMutex { result.engineName = name  }
        try {
            doWithMutex { eval() }
            Debug.log("sourceString:$sourceString $sourceLanguage -> $targetLanguage ")
            Debug.log("开始执行 madeURL 方法……")
            val url = madeURL()
            Debug.log("成功！url：${url.emptyString()}")
            Debug.log("开始执行 getBasicText 方法……")
            val basicText = getBasicText(url)
            Debug.log("成功！basicText：${basicText.emptyString()}")
            Debug.log("开始执行 getFormattedResult 方法……")
            doWithMutex {
                getFormattedResult(basicText)
                Debug.log("成功！result:$result")
            }
            Debug.log("插件执行完毕！")
        } catch (exception: ScriptException) {
            Debug.log(exception.messageWithDetail)
            doWithMutex { result.setBasicResult("翻译错误：${exception.messageWithDetail}") }
            return
        } catch (exception: TranslationException) {
            Debug.log("翻译过程中发生错误！原因如下：\n${exception.message}")
            doWithMutex { result.setBasicResult("翻译错误：${exception.message}") }
            return
        } catch (e: Exception) {
            Debug.log("出错:${e.message}")
            doWithMutex { result.setBasicResult("翻译错误：${e.message}") }
            return
        }
    }

    private fun eval() {
        with(SCRIPT_ENGINE){
            put("sourceLanguage", sourceLanguage)
            put("targetLanguage", targetLanguage)
            put("sourceString", sourceString)
            put("result_${name.hashCode().absoluteValue}", result)
        }
        jsEngine.eval()
    }
}