package com.funny.translation.debug

object Debug {
    private val targets : ArrayList<DebugTarget> = arrayListOf()
    fun addTarget(target: DebugTarget){
        if(!targets.contains(target)){
            targets.add(target)
        }
    }

    fun log(
        text : CharSequence,
        tempSource : String?= null
    ){
        targets.forEach { it.appendLog("[${tempSource?:it.source}] $text") }
    }

    fun clear(){
        targets.clear()
    }

    interface DebugTarget{
        val source : String
        fun appendLog(text : CharSequence)
    }
}