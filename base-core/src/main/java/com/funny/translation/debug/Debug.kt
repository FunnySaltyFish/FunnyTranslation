package com.funny.translation.debug

object Debug {
    private val targets : ArrayList<DebugTarget> = arrayListOf()
    fun addTarget(target: DebugTarget){
        if(!targets.contains(target)){
            targets.add(target)
        }
    }

    fun log(text : CharSequence){
        targets.forEach { it.appendLog("[${it.source}] $text") }
    }

    fun clear(){
        targets.clear()
    }

    interface DebugTarget{
        val source : String
        fun appendLog(text : CharSequence)
    }
}