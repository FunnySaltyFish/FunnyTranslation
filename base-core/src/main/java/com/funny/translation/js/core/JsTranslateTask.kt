package com.funny.translation.js.core

import com.funny.translation.debug.Debug
import com.funny.translation.js.JsEngine
import com.funny.translation.js.config.BaseConfig.Companion.JS_ENGINE_KIND
import com.funny.translation.js.config.BaseConfig.Companion.SCRIPT_ENGINE
import com.funny.translation.js.extentions.messageWithDetail
import com.funny.translation.trans.CoreTranslationTask
import com.funny.translation.trans.TranslationException
import com.funny.translation.trans.TranslationResult
import org.mozilla.javascript.NativeObject
import org.mozilla.javascript.RhinoException
import java.lang.Exception
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
        val obj = jsEngine.evalFunnyJSFunction(
            "getBasicText"
        )
        return when (obj) {
            is NativeObject -> obj.toString()
            else -> obj.toString()
        }
    }

    override fun getFormattedResult(basicText: String): TranslationResult {
        return jsEngine.evalFunnyJSFunction("getFormattedResult", basicText) as TranslationResult
    }

    override fun madeURL(): String {
        val obj = jsEngine.evalFunnyJSFunction(
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
                jsEngine.evalFunnyJSFunction("isOffline") as Boolean
            } catch (e: Exception) {
                true
            }


    override val engineKind: Short
        get() = JS_ENGINE_KIND


    override fun translate(mode: Short) {
        try {
            eval()
            Debug.log("开始执行 madeURL 方法……")
            val url = madeURL()
            Debug.log("成功！url：$url")
            Debug.log("开始执行 getBasicText 方法……")
            val basicText = getBasicText(url)
            Debug.log("成功！basicText:$basicText")
            Debug.log("开始执行 getFormattedResult 方法……")
            result = getFormattedResult(basicText)
            Debug.log("成功！result:$basicText")
        } catch (exception: ScriptException) {
            return
        } catch (exception: TranslationException) {
            Debug.log("翻译过程中发生错误！原因如下：\n${exception.message}")
            return
        } catch (e : Exception){
            Debug.log("出错:${e.message}")
            return
        }
    }

    private fun eval() {
        with(SCRIPT_ENGINE) {
            put("funny",this@JsTranslateTask)
            put("sourceLanguage",sourceLanguage)
            put("targetLanguage",targetLanguage)
            put("sourceString",sourceString)
            put("result",TranslationResult(engineKind))
        }
        jsEngine.eval()
    }

}