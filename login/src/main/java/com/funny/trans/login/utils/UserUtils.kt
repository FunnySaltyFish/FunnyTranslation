package com.funny.trans.login.utils

object UserUtils {
    fun isValidUsername(username: String): Boolean {
        return "^[\\w\\W\\u4e00-\\u9fff_]{3,16}\$".toRegex().matches(username)
    }
}