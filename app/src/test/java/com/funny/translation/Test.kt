package com.funny.translation

import com.funny.translation.bean.Consts
import com.funny.translation.translation.TranslationBaiduNormal
import com.funny.translation.translation.TranslationGoogleNormal
import com.funny.translation.translation.TranslationYouDaoNormal
import junit.framework.TestCase
import org.junit.Test

class TestClass {
    @Test
    fun repeatTest(){
        val sourceString = "年后"
        val sourceLanguage = Consts.LANGUAGE_CHINESE
        val targetLanguage = Consts.LANGUAGE_ENGLISH
        val task1 = TranslationYouDaoNormal("我觉得你太帅了",sourceLanguage,targetLanguage)
        val task2 = TranslationYouDaoNormal("我不这样觉得", sourceLanguage, targetLanguage)
        task1.translate()
        println(task1.result)

        task2.translate()

        val result1 = task1.result
        val result2 = task2.result

        println(result1 == result2)

        println(result1)
        println(result2)
    }
}