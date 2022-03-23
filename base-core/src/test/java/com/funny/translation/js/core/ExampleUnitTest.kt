package com.funny.translation.js.core

import android.util.Log
import com.funny.translation.debug.Debug
import com.funny.translation.debug.DefaultDebugTarget
import com.funny.translation.js.extentions.show
import org.junit.Test

import org.junit.Assert.*
import org.mozilla.javascript.*
import org.mozilla.javascript.Function
import javax.script.Invocable
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.SimpleBindings

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
data class Bean(var name:String)

class ExampleUnitTest : JsInterface{
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun resultText(){
        //为什么属性会被复用？？？？？

    }

    lateinit var obj : NativeObject
    lateinit var invocable : Invocable

    @Test
    fun jsEngineTest(){
        val scriptEngine: ScriptEngine by lazy {
            ScriptEngineManager().getEngineByName("rhino")
        }
        Debug.addTarget(object : Debug.DebugTarget{
            override val source: String
                get() = "Log"

            override fun appendLog(text: CharSequence) {
                println(text.toString())
            }
        })

        val code = """
            var obj = {
                "c" : "Hello",
                "d" : function(name){
                    println("d:"+name)
                    funny.log("log:d:"+name)
                    return name
                },
                "e" : true,
                "f" : undefined,
                "g" : {
                    "h":"asdasd",
                    "i":{
                        "j" : [[1,2,3],[4,5,6]]
                    }
                }
            }
            function s(){
                println("damn it")
            }
            
            var jsStr = "张三";
            
            //typeof ->  object
            var javaStr = bean.getName();
            println(typeof(jsStr))
            println(typeof(javaStr))
            println(jsStr === (javaStr+""))
            
            bean.setName("李四");
            println(bean);
            
            println("Finish Load");
            //println(funny.get("https://www.baidu.com",null));
        """.trimIndent()
        scriptEngine.put("funny",this)
        val bean = Bean("张三")
        scriptEngine.put("bean",bean)
        scriptEngine.eval(code)


        println(bean)
        invocable = scriptEngine as Invocable
        obj = scriptEngine.get("obj") as NativeObject
        println(
            invocable.invokeMethod(obj,"d","惊叹号")
            //invokeMethod("d",java.lang.String("惊叹号"))
        )
        println(obj.show())
        //invocable.invokeFunction("s")

//        val func = obj["d"] as Function
//        val s = scriptEngine.factory.getMethodCallSyntax("obj","d")
        //func.call(Context.getCurrentContext(),,func, arrayOf())

        println("Finish")

    }

    private fun invokeMethod(name: String, vararg arguments:Object){
        invocable.invokeMethod(obj,name,arguments)
    }

    fun NativeObject.show(
        deep : Int = 1
    ) : String{
        val stringBuilder = StringBuilder("{\n")
        this.ids.forEach { id->
            if (id is String){
                val v = this[id]
                val value = when(v){
                    is NativeObject -> v.show(deep+1)
                    is NativeArray -> v.show
                    else -> v?.javaClass?.name
                }
                //println("$id->${this[id]?.javaClass?.name}")
                for (i in 0 until deep)stringBuilder.append("\t")
                stringBuilder.append("${id}:${value},\n") //删去多余的","
            }
        }
        stringBuilder.deleteCharAt(stringBuilder.length-2)
        for (i in 0 until deep-1)stringBuilder.append("\t")
        stringBuilder.append("}")
        return stringBuilder.toString()
    }
}