package com.funny.translation.js.bean

data class JsBean(
    var id : Int = 0,
    val fileName : String = "Plugin",
    val code : String = "",
    val author : String = "Author",
    val version : Int = 1,
    val description : String = "",
    var enabled : Int = 0,
)
