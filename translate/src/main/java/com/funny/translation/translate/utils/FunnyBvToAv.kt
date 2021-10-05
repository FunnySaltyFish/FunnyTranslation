package com.funny.translation.translate.utils

import kotlin.math.floor
import kotlin.math.pow

object FunnyBvToAv {
    //改编自 https://github.com/MOHJ0558/Bilibili_Conversion_between_av_and_BV/blob/master/avBV.cpp
    //该算法的最初版本使用极为罕见的WTFPL协议，感谢开源！
    //java 版 by FunnySaltyFish 2020.4.4
    var xorn = 177451812L
    var add = 8728348608L
    var s = intArrayOf(11, 10, 3, 8, 4, 6)
    var TABLE = "fZodR9XQDSUm21yCkr6zBqiveYah8bt4xsWpHnJE7jL5VG3guMTKNPAwcF"
    var tr = LongArray(123)
    fun dec(x: String): String { //传 bv...
        var r: Long = 0
        for (i in 0..5) {
            r += Math.pow(58.0, i.toDouble()).toLong() * tr[x[s[i]].code]
        }
        val av = r - add xor xorn
        return "av$av"
    }

    fun enc(x: Long): String { //仅传 av号
        var x = x
        x = (x xor xorn) + add
        val r = "BV1  4 1 7  ".toCharArray()
        for (i in 0..5) {
            r[s[i]] = TABLE[floor((x / 58.0.pow(i.toDouble()).toLong() % 58).toDouble())
                .toInt()]
        }
        return String(r)
    }

    init {
        for (i in 0..57) {
            tr[TABLE[i].code] = i.toLong()
        }
    }
}