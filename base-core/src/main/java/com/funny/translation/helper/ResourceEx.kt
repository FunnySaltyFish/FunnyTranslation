package com.funny.translation.helper

import com.funny.translation.BaseApplication

fun string(id: Int) = BaseApplication.ctx.getString(id)
fun string(id: Int, vararg args: Any) = BaseApplication.ctx.getString(id, *args)