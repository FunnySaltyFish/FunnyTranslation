package com.funny.translation.translate.utils

import java.lang.StringBuilder
import java.text.DecimalFormat
import java.util.regex.Pattern

object StringUtil {
    fun extraChinese(sourceText: String): String {
        val sb = StringBuilder()
        val cn = Pattern.compile("[\\u4E00-\\u9FA5]+")
        val enMatcher = cn.matcher(sourceText)
        while (enMatcher.find()) {
            sb.append(enMatcher.group(0))
        }
        return sb.toString()
    }

    fun unicodeToString(str: String): String {
        var str = str
        val pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))")
        val matcher = pattern.matcher(str)
        var ch: Char
        while (matcher.find()) {
            val group = matcher.group(2)
            ch = group!!.toInt(16).toChar()
            val group1 = matcher.group(1)
            str = str.replace(group1!!, ch.toString() + "")
        }
        return str
    }

    fun isUnicode(str: String): Boolean { //是否是unicode
        val pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))")
        val matcher = pattern.matcher(str)
        return matcher.matches()
    }

    fun isNumber(str: String): Boolean {
        val p = Pattern.compile("^\\d+$")
        val m = p.matcher(str)
        return m.matches()
    }

    fun findAv(str: String): Long { //返回数字
        val p = Pattern.compile("av(\\d+)", Pattern.CASE_INSENSITIVE)
        val m = p.matcher(str)
        return if (m.find()) {
            java.lang.Long.valueOf(m.group(1)!!)
        } else -1
    }

    fun findBv(str: String): String { //返回 bv...
        val p = Pattern.compile("bv\\w+", Pattern.CASE_INSENSITIVE)
        val m = p.matcher(str)
        return if (m.find()) {
            m.group(0)!!
        } else ""
    }

    fun convertIntToSize(size: Int): String {
        val GB = 1024 * 1024 * 1024
        val MB = 1024 * 1014
        val KB = 1024
        val df = DecimalFormat("0.00")
        return when {
            size / GB >= 1 -> df.format(size / GB.toFloat()) + "GB"
            size / MB >= 1 -> df.format(size / MB.toFloat()) + "MB"
            size / KB >= 1 -> df.format(size / KB.toFloat()) + "KB"
            else -> size.toString() + "B"
        }
    }
}