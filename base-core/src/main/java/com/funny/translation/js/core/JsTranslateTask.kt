package com.funny.translation.js.core

import com.funny.translation.debug.Debug
import com.funny.translation.js.JsEngine
import com.funny.translation.js.config.JsConfig.Companion.INVOCABLE
import com.funny.translation.js.config.JsConfig.Companion.JS_ENGINE_KIND
import com.funny.translation.js.config.JsConfig.Companion.SCRIPT_ENGINE
import com.funny.translation.js.extentions.messageWithDetail
import com.funny.translation.trans.CoreTranslationTask
import com.funny.translation.trans.TranslationException
import com.funny.translation.trans.TranslationResult
import java.lang.Exception
import javax.script.ScriptException

private const val TAG = "JsTranslateTask"

class JsTranslateTask(
    val jsEngine: JsEngine,
    sourceString: String,
    sourceLanguage: Short,
    targetLanguage: Short
) :
    CoreTranslationTask(sourceString, sourceLanguage, targetLanguage), JsInterface {

    override fun getBasicText(url: String): String {
        val obj = INVOCABLE.invokeMethod(jsEngine.funnyJS, "getBasicText", url)
        //Log.d(TAG, "getBasicText: ${obj is String}")
        return obj as String
    }

    override fun getFormattedResult(basicText: String): TranslationResult {
        return INVOCABLE.invokeMethod(
            jsEngine.funnyJS,
            "getFormattedResult",
            basicText
        ) as TranslationResult
    }

    override fun madeURL(): String {
        val obj = INVOCABLE.invokeMethod(jsEngine.funnyJS, "madeURL")
        return obj as String
    }

    override val isOffline: Boolean
        get() =
            try {
                jsEngine.evalFunnyJSFunction("isOffline") as Boolean
            } catch (e: Exception) {
                true
            }


    override val engineKind: Short
        get() = JS_ENGINE_KIND


    override fun translate(mode: Short) {
        fun String.emptyString() = if (this.isEmpty()) " [空字符串]" else this

        try {
            eval()
            Debug.log("开始执行 madeURL 方法……")
            val url = madeURL()
            Debug.log("成功！url：${url.emptyString()}")
            Debug.log("开始执行 getBasicText 方法……")
            val basicText = getBasicText(url)
            Debug.log("成功！basicText：${basicText.emptyString()}")
            Debug.log("开始执行 getFormattedResult 方法……")
            result = getFormattedResult(basicText)
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
            put("funny", this@JsTranslateTask)
            put("sourceLanguage", sourceLanguage)
            put("targetLanguage", targetLanguage)
            put("sourceString", sourceString)
            put("result", TranslationResult(engineKind))
        }
        jsEngine.eval()
    }

}