package com.funny.translation.js.bean

data class JsBean(
    val id : Int,
    val fileName : String = "Plugin",
    val code : String = "",
    val author : String = "Author",
    val version : Int = 1,
    val description : String = "",
    val enabled : Int = 0,
)
