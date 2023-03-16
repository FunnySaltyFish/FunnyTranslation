package com.funny.translation.js

import androidx.annotation.Keep
import com.funny.translation.debug.Debug
import com.funny.translation.js.bean.JsBean
import com.funny.translation.js.config.JsConfig
import com.funny.translation.js.config.JsConfig.Companion.INVOCABLE
import com.funny.translation.js.config.JsConfig.Companion.SCRIPT_ENGINE
import com.funny.translation.js.core.JsInterface
import com.funny.translation.js.extentions.messageWithDetail
import com.funny.translation.network.OkHttpUtils
import com.funny.translation.translate.Language
import com.funny.translation.translate.allLanguages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mozilla.javascript.NativeArray
import org.mozilla.javascript.NativeObject
import org.mozilla.javascript.RhinoException
import javax.script.ScriptException
import kotlin.math.absoluteValue

@Keep
class JsEngine(val jsBean: JsBean) : JsInterface {

    private val patternResult = Regex("(\\W)result\\.")

    lateinit var funnyJS : NativeObject
    @Throws(ScriptException::class)
    fun eval(){
        with(SCRIPT_ENGINE) {
            put("funny", this@JsEngine)
            put("http", OkHttpUtils)
            Language.values().forEach {
                put("LANGUAGE_${it.name}",it)
            }
        }
        // 为了实现多引擎同步翻译，替换 result 为 result_${engineName.hashCode()}
        val code = jsBean.code.replace(patternResult,"\$1result_${jsBean.fileName.hashCode().absoluteValue}.")
        SCRIPT_ENGINE.eval(code)
        funnyJS = getProperty("FunnyJS") as NativeObject
    }

    @Throws(ScriptException::class,NoSuchMethodException::class)
    fun evalFunction(name : String,vararg arguments : Any) : Any? {
        return try{
            INVOCABLE.invokeFunction(name,arguments)
        }catch (e : NoSuchMethodException){
            log("函数[$name]不存在！")
            throw e
        }catch (e : ScriptException){
            log(e.messageWithDetail)
            throw e
        }catch (e : Exception){
            log("执行函数[${name}]时产生错误！\n${e.message}")
            throw e
        }
    }

    //@Throws(ScriptException::class,NoSuchMethodException::class)
    //不能用这个方法，否则传给JS的类型不对
//    fun evalFunnyJSFunction(name : String,vararg arguments : Object) : Any? {
//        return try{
//            INVOCABLE.invokeMethod(funnyJS,name,arguments)
//        }catch (e : NoSuchMethodException){
//            log("方法[FunnyJS.$name]不存在！")
//            throw e
//        }catch (e : ScriptException){
//            log(e.messageWithDetail)
//            throw e
//        }catch (e : Exception){
//            log("执行方法[FunnyJS.${name}]时产生错误！\n${e.message}")
//            throw e
//        }
//    }

    private fun getProperty(name : String) : Any? {
        return SCRIPT_ENGINE.get(name)
    }

    fun getId() = jsBean.id

    val isOffline
        get() = jsBean.isOffline

    suspend fun loadBasicConfigurations(
        onSuccess: ()->Unit,
        onError : (Throwable) -> Unit
    ){
        withContext(Dispatchers.IO){
            kotlin.runCatching {
                JsManager.currentRunningJsEngine = this@JsEngine
                with(SCRIPT_ENGINE){
                    put("funny",this@JsEngine)
                }
                log("插件引擎v${JsConfig.JS_ENGINE_VERSION}启动完成")
                log("开始加载插件……")
                //log("源代码：\n${jsBean.code}")
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
                    minSupportVersion = getFunnyOrDefault("minSupportVersion", 2)
                    maxSupportVersion = getFunnyOrDefault("maxSupportVersion", 9999)
                    targetSupportVersion = getFunnyOrDefault("targetSupportVersion", JsConfig.JS_ENGINE_VERSION)
                    debugMode = getFunnyOrDefault("debugMode", false)
                    this.isOffline = getFunnyOrDefault("isOffline", false)
                    supportLanguages = getFunnyListOrDefault("supportLanguages", allLanguages)
                }
            }.onSuccess {
                log("插件加载完毕！")
                log(JsConfig.DEBUG_DIVIDER)
                log(" 【${jsBean.fileName}】 版本号：${jsBean.version}  作者：${jsBean.author}")
                log("  ---> ${jsBean.description}")
                log("支持的语言：${jsBean.supportLanguages}")
                log(JsConfig.DEBUG_DIVIDER)
                onSuccess()
            }.onFailure{  e ->
                when(e){
                    is ScriptException -> log("加载插件时出错！${e.messageWithDetail}")
                    is RhinoException -> log("加载插件时出错！${e.messageWithDetail}")
                    is TypeCastException -> log("加载插件时出错！${e.message}\n【建议检查配置文件名称及返回值是否正确】")
                    is NullPointerException -> log("加载插件时出错！${e.message}\n【建议检查配置文件名称及返回值是否正确】")
                    is Exception -> log("加载插件时出错！${e.message}")
                }
                onError(e)
            }
        }
    }

    private fun <T> getFunnyOrDefault(key : String , default : T) : T{
        return try{
            (funnyJS[key]?:default) as T
        }catch (e : Exception){
            default
        }
    }

    private fun <E> getFunnyListOrDefault(key : String , default : List<E>) : List<E>{
        return try{
            funnyJS[key]?:return default
            val nativeArray = funnyJS[key] as NativeArray
            val result = arrayListOf<E>()
            for (each in nativeArray){
                result.add(each as E)
            }
            result
        }catch (e : Exception){
            e.printStackTrace()
            default
        }
    }
    
    private fun log(text: String) = Debug.log(text, "[DebugLog-${jsBean.fileName}]")
}