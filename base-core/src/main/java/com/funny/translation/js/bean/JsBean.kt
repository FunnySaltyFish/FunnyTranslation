package com.funny.translation.js.bean

data class JsBean(
    var id : Int = 0,
    var fileName : String = "Plugin",
    var code : String = "",
    var author : String = "Author",
    var version : Int = 1,
    var description : String = "",
    var enabled : Int = 0,
)
