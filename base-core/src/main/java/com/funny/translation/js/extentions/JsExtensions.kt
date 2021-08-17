package com.funny.translation.js.extentions

import org.mozilla.javascript.NativeArray
import org.mozilla.javascript.NativeObject
import org.mozilla.javascript.RhinoException
import javax.script.ScriptException

val ScriptException.messageWithDetail
    get() = "第${this.lineNumber}行、第${this.columnNumber}列发生错误：\n${this.message}"

val RhinoException.messageWithDetail
    get() = "第${this.lineNumber()}行、第${this.columnNumber()}列发生错误：\n${this.message}"

val NativeArray.show
    get() = this.toArray().joinToString(prefix = "[",postfix = "]")

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