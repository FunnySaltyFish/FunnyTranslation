package com.funny.translation

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.funny.translation.bean.Consts
import com.funny.translation.translation.TranslationBaiduNormal
import com.funny.translation.translation.TranslationYouDaoNormal
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResultTest{
    @Test
    fun testRepeat(){
        val sourceString = "年后"
        val sourceLanguage = Consts.LANGUAGE_CHINESE
        val targetLanguage = Consts.LANGUAGE_ENGLISH
        val task1 = TranslationBaiduNormal(sourceString,sourceLanguage,targetLanguage)
        val task2 = TranslationYouDaoNormal(sourceString, sourceLanguage, targetLanguage)
        task1.translate()
        task2.translate()

        val result1 = task1.result
        val result2 = task2.result

        println(result1)
        println(result2)
        assert(
            true
        )
    }
}