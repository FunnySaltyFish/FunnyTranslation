package com.funny.translation.js.core

import com.funny.translation.debug.Debug
import com.funny.translation.js.JsEngine
import com.funny.translation.js.config.JsConfig.Companion.INVOCABLE
import com.funny.translation.js.config.JsConfig.Companion.JS_ENGINE_KIND
import com.funny.translation.js.config.JsConfig.Companion.SCRIPT_ENGINE
import com.funny.translation.js.extentions.messageWithDetail
import com.funny.translation.trans.CoreTranslationTask
import com.funny.translation.trans.Language
import com.funny.translation.trans.TranslationException
import java.lang.Exception
import javax.script.ScriptException

private const val TAG = "JsTranslateTask"

class JsTranslateTask(
    val jsEngine: JsEngine,
    sourceString: String,
    sourceLanguage: Language,
    targetLanguage: Language
) :
    CoreTranslationTask(sourceString, sourceLanguage, targetLanguage) {

    override val languageMapping: Map<Language, String>
        get() = mapOf()

    override val supportLanguages: List<Language>
        get() = TODO("Not yet implemented")

    override val name: String
        get() = jsEngine.jsBean.fileName

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

    override fun translate(mode: Int) {
        fun String.emptyString() = if (this.isEmpty()) " [空字符串]" else this
        result.engineName = name
        try {
            eval()
            Debug.log("开始执行 madeURL 方法……")
            val url = madeURL()
            Debug.log("成功！url：${url.emptyString()}")
            Debug.log("开始执行 getBasicText 方法……")
            val basicText = getBasicText(url)
            Debug.log("成功！basicText：${basicText.emptyString()}")
            Debug.log("开始执行 getFormattedResult 方法……")
            getFormattedResult(basicText)
            Debug.log("成功！result:$result")

            Debug.log("插件执行完毕！")
        } catch (exception: ScriptException) {
            Debug.log(exception.messageWithDetail)
            return
        } catch (exception: TranslationException) {
            Debug.log("翻译过程中发生错误！原因如下：\n${exception.message}")
            return
        } catch (e: Exception) {
            Debug.log("出错:${e.message}")
            return
        }
    }

    private fun eval() {
        with(SCRIPT_ENGINE) {
            put("funny", jsEngine)
            put("sourceLanguage", sourceLanguage)
            put("targetLanguage", targetLanguage)
            put("sourceString", sourceString)
            put("result", result)
            Language.values().forEach {
                put("LANGUAGE_${it.name}",it.id)
            }
        }
        jsEngine.eval()
    }

}