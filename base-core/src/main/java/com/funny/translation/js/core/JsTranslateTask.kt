package com.funny.translation.js.core

import com.funny.translation.debug.Debug
import com.funny.translation.js.JsEngine
import com.funny.translation.js.config.BaseConfig.Companion.JS_ENGINE_KIND
import com.funny.translation.js.extentions.messageWithDetail
import com.funny.translation.trans.CoreTranslationTask
import com.funny.translation.trans.TranslationException
import com.funny.translation.trans.TranslationResult
import org.mozilla.javascript.NativeObject
import org.mozilla.javascript.RhinoException
import javax.script.ScriptException
import javax.script.SimpleBindings

class JsTranslateTask(
    val jsEngine: JsEngine,
    sourceString: String,
    sourceLanguage: Short,
    targetLanguage: Short
) :
    CoreTranslationTask(sourceString, sourceLanguage, targetLanguage), JsInterface {

    override fun getBasicText(url: String): String {
        val obj = jsEngine.evalFunction(
            "getBasicText"
        )
        return when (obj) {
            is NativeObject -> obj.toString()
            else -> obj.toString()
        }
    }

    override fun getFormattedResult(basicText: String): TranslationResult {
        return jsEngine.evalFunction("getFormattedResult", basicText) as TranslationResult
    }

    override fun madeURL(): String {
        val obj = jsEngine.evalFunction(
            "madeURL"
        )
        return when (obj) {
            is NativeObject -> obj.toString()
            else -> obj.toString()
        }
    }

    override val isOffline: Boolean
        get() =
            try {
                jsEngine.evalFunction("isOffline") as Boolean
            } catch (e: ScriptException) {
                Debug.log("[isOffline] ${e.messageWithDetail}")
                true
            } catch (e: NoSuchMethodException) {
                Debug.log("[isOffline]方法不存在！")
                true
            }


    override val engineKind: Short
        get() = JS_ENGINE_KIND


    override fun translate(mode: Short) {
        try {
            eval()
            val url = madeURL()
            val basicText = getBasicText(url)
            result = getFormattedResult(basicText)
        } catch (exception: RhinoException) {
            Debug.log("Js于第${exception.lineNumber()}行、第${exception.columnNumber()}列发生错误！原因如下：\n${exception.message}")
            return
        } catch (exception: TranslationException) {
            Debug.log("翻译过程中发生错误！原因如下：\n${exception.message}")
            return
        }
    }

    private fun eval() {
        val bindings = SimpleBindings().apply {
            this["funny"] = this@JsTranslateTask
            this["sourceLanguage"] = sourceLanguage
            this["targetLanguage"] = targetLanguage
            this["sourceString"] = sourceString
            this["result"] = TranslationResult(engineKind)
        }
        jsEngine.eval(bindings)
    }

}