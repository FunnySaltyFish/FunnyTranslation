package com.funny.translation.js

import com.funny.translation.debug.Debug
import com.funny.translation.helper.coroutine.Coroutine
import com.funny.translation.js.bean.JsBean
import com.funny.translation.js.config.JsConfig
import com.funny.translation.js.config.JsConfig.Companion.INVOCABLE
import com.funny.translation.js.config.JsConfig.Companion.SCRIPT_ENGINE
import com.funny.translation.js.core.JsInterface
import com.funny.translation.js.extentions.messageWithDetail
import com.funny.translation.trans.allLanguages
import org.mozilla.javascript.NativeObject
import org.mozilla.javascript.RhinoException
import java.util.*
import javax.script.ScriptException

class JsEngine(val jsBean: JsBean) : JsInterface {

    lateinit var funnyJS : NativeObject
    @Throws(ScriptException::class)
    fun eval(){
        SCRIPT_ENGINE.eval(jsBean.code)
        funnyJS = getProperty("FunnyJS") as NativeObject
    }

    @Throws(ScriptException::class,NoSuchMethodException::class)
    fun evalFunction(name : String,vararg arguments : Any) : Any? {
        return try{
            INVOCABLE.invokeFunction(name,arguments)
        }catch (e : NoSuchMethodException){
            Debug.log("函数[$name]不存在！")
            throw e
        }catch (e : ScriptException){
            Debug.log(e.messageWithDetail)
            throw e
        }catch (e : Exception){
            Debug.log("执行函数[${name}]时产生错误！\n${e.message}")
            throw e
        }
    }

    //@Throws(ScriptException::class,NoSuchMethodException::class)
    //不能用这个方法，否则传给JS的类型不对
//    fun evalFunnyJSFunction(name : String,vararg arguments : Object) : Any? {
//        return try{
//            INVOCABLE.invokeMethod(funnyJS,name,arguments)
//        }catch (e : NoSuchMethodException){
//            Debug.log("方法[FunnyJS.$name]不存在！")
//            throw e
//        }catch (e : ScriptException){
//            Debug.log(e.messageWithDetail)
//            throw e
//        }catch (e : Exception){
//            Debug.log("执行方法[FunnyJS.${name}]时产生错误！\n${e.message}")
//            throw e
//        }
//    }

    private fun getProperty(name : String) : Any? {
        return SCRIPT_ENGINE.get(name)
    }

    fun getId() = jsBean.id

    val isOffline
        get() = jsBean.isOffline

    fun loadBasicConfigurations(
        onSuccess: ()->Unit,
        onError : (Throwable) -> Unit
    ){
        Coroutine.async {
            JsManager.currentRunningJsEngine = this@JsEngine
            with(SCRIPT_ENGINE){
                put("funny",this@JsEngine)
            }
            Debug.log("插件引擎v${JsConfig.JS_ENGINE_VERSION}启动完成")
            Debug.log("开始加载插件……")
            //Debug.log("源代码：\n${jsBean.code}")
            eval()
            /**
             * FunnyJS : IdScriptableObject
             * isOffline : Iter...Function
             */
            with(jsBean){
                author = funnyJS["author"] as String
                description = funnyJS["description"] as String
                fileName = funnyJS["name"] as String
                version = (funnyJS["version"] as Double).toInt()
                minSupportVersion = (funnyJS["minSupportVersion"] as Double).toInt()
                maxSupportVersion = (funnyJS["maxSupportVersion"] as Double).toInt()
                debugMode = funnyJS["debugMode"] as Boolean
                this.isOffline = funnyJS["isOffline"] as Boolean
                supportLanguages = getFunnyOrDefault("supportLanguage", allLanguages)
                //Debug.log(funnyJS.toString())
            }
        }.onSuccess {
            Debug.log("插件加载完毕！")
            Debug.log(JsConfig.DEBUG_DIVIDER)
            Debug.log(" 【${jsBean.fileName}】 版本号：${jsBean.version}  作者：${jsBean.author}")
            Debug.log("  ---> ${jsBean.description}")
            Debug.log(JsConfig.DEBUG_DIVIDER)
            onSuccess()
        }.onError {  e ->
            when(e){
                is ScriptException -> Debug.log("加载插件时出错！${e.messageWithDetail}")
                is RhinoException -> Debug.log("加载插件时出错！${e.messageWithDetail}")
                is TypeCastException -> Debug.log("加载插件时出错！${e.message}\n【建议检查配置文件名称及返回值是否正确】")
                is NullPointerException -> Debug.log("加载插件时出错！${e.message}\n【建议检查配置文件名称及返回值是否正确】")
                is Exception -> Debug.log("加载插件时出错！${e.message}")
            }
            onError(e)
        }
    }

    private fun<T> getFunnyOrDefault(key : String , default : T) : T{
        return try{
            funnyJS[key] as T
        }catch (e : Exception){
            default
        }
    }


}