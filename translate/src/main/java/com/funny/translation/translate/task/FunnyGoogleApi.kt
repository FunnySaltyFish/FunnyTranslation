package com.funny.translation.translate.task

import java.lang.Exception

object FunnyGoogleApi {
    //部分参考自https://blog.csdn.net/u013070165/article/details/85112935
    // 实现js的charAt方法
    fun charAt(obj: Any, index: Int): Char {
        return obj.toString()[index]
    }

    // 实现js的charCodeAt方法
    fun charCodeAt(obj: Any, index: Int): Int {
        return obj.toString()[index].toInt()
    }

    // 实现js的Number方法
    fun Number(cc: Any): Int {
        return try {
            val a = cc.toString().toLong()
            if (a > 2147483647) (a - 4294967296L).toInt() else if (a < -2147483647) (a + 4294967296L).toInt() else a.toInt()
        } catch (ex: Exception) {
            0
        }
    }

    fun b(a: Long, b: String): String {
        var a = a
        var d = 0
        while (d < b.length - 2) {
            val c = b[d + 2]
            val c0 = if ('a' <= c) charCodeAt(c, 0) - 87 else Number(c)
            val c1 = if ('+' == b[d + 1]) a shr c0 else a shl c0
            a = if ('+' == b[d]) a + c1 and 4294967295L else a xor c1
            d += 3
        }
        a = Number(a).toLong()
        return a.toString() + ""
    }

    //关键，获取tk
    fun tk(a: String, TKK: String): String {
        val e = TKK.split("\\.").toTypedArray()
        var d = 0
        var h = 0
        val g = IntArray(a.length * 3)
        h = Number(e[0])
        var f = 0
        while (f < a.length) {
            var c = charCodeAt(a, f)
            if (128 > c) {
                g[d++] = c
            } else {
                if (2048 > c) {
                    g[d++] = c shr 6 or 192
                } else {
                    if (55296 == c and 64512 && f + 1 < a.length && 56320 == charCodeAt(
                            a,
                            f + 1
                        ) and 64512
                    ) {
                        c = 65536 + (c and 1023 shl 10) + charCodeAt(a, ++f) and 1023
                        g[d++] = c shr 18 or 240
                        g[d++] = c shr 12 and 63 or 128
                    } else {
                        g[d++] = c shr 12 or 224
                        g[d++] = c shr 6 and 63 or 128
                        g[d++] = c and 63 or 128
                    }
                }
            }
            f++
        }
        var gl = 0
        for (b in g) {
            if (b != 0) {
                gl++
            }
        }
        val g0 = IntArray(gl)
        gl = 0
        for (c in g) {
            if (c != 0) {
                g0[gl] = c
                gl++
            }
        }
        var aa = h.toLong()
        d = 0
        while (d < g0.size) {
            aa += g0[d]
            aa = Number(b(aa, "+-a^+6")).toLong()
            d++
        }
        aa = Number(b(aa, "+-3^+b+-f")).toLong()
        var bb = aa xor Number(e[1])
            .toLong()
        aa = bb
        aa = aa + bb
        bb = aa - bb
        aa = aa - bb
        if (0 > aa) {
            aa = (aa and 2147483647) + 2147483648L
        }
        aa %= 1e6.toLong()
        return aa.toString() + "" + "." + (aa xor h.toLong())
    }

    fun showArray(arr: Array<Array<String?>>) {
        for (arr1 in arr) {
            for (str in arr1) {
                print(str)
                print(" ")
            }
            println("")
        }
    }
}