package com.funny.translation.translate

import com.funny.translation.translate.extentions.md5
import com.funny.translation.translate.task.MD5
import com.funny.translation.translate.utils.SortResultUtils.get
import org.junit.Test

import org.junit.Assert.*
import java.lang.Integer.parseInt

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        //assertEquals(4, 2 + 2)
//        val text : String = "Hello World"
//        println(text.md5)
//        println(MD5.md5(text))
        val hash = hashMapOf(
            "k" to "1",
            "v" to "2"
        )
        println(hash.get("k","c"))
        println(hash.get("v","cc"))
        println(hash.get("c","ccc"))
        val a: Int? = try { parseInt("ada") } catch (e: NumberFormatException) { 777 }
        println(a)
    }


}