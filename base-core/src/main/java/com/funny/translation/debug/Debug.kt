package com.funny.translation.debug

/**
 * 用于 JS 调试输出
 */
object Debug {
    private val targets : ArrayList<DebugTarget> = arrayListOf()
    fun addTarget(target: DebugTarget){
        if(!targets.contains(target)){
            targets.add(target)
        }
    }

    fun removeTarget(target : DebugTarget){
        targets.remove(target)
    }

    fun log(
        text : CharSequence,
        tempSource : String?= null,
        print : Boolean = true
    ){
        targets.forEach { if(print) it.appendLog("[${tempSource?:it.source}] $text") }
    }

    fun clear(){
        targets.clear()
    }

    interface DebugTarget{
        val source : String
        fun appendLog(text : CharSequence)
    }
}