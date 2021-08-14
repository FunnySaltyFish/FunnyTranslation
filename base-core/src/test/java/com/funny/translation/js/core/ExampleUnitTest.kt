package com.funny.translation.js.core

import org.junit.Test

import org.junit.Assert.*
import org.mozilla.javascript.Context
import org.mozilla.javascript.Function
import org.mozilla.javascript.NativeObject
import org.mozilla.javascript.Scriptable
import javax.script.Invocable
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.SimpleBindings

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

class ExampleUnitTest : JsInterface{
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun jsEngineTest(){
        val scriptEngine: ScriptEngine by lazy {
            ScriptEngineManager().getEngineByName("rhino")
        }
        val code = """
            var obj = {
                "c" : "Hello",
                "d" : function(name){
                    println("d:"+name)
                    return "d"
                }
            }
            function s(){
                println("damn it")
            }
            
            println("Finish Load");
            //println(funny.get("https://www.baidu.com",null));
        """.trimIndent()
        val bindings = SimpleBindings().apply {
            this["funny"] = this@ExampleUnitTest
        }
        scriptEngine.put("funny",this)
        scriptEngine.eval(code)
        val invocable = scriptEngine as Invocable
        //invocable.invokeFunction("s")
        val obj = scriptEngine.get("obj") as NativeObject
        val func = obj["d"] as Function
        val s = scriptEngine.factory.getMethodCallSyntax("obj","d")
        //func.call(Context.getCurrentContext(),,func, arrayOf())

        println("Finish")

    }
}