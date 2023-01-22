package com.funny.translation.translate

import android.content.res.Resources
import androidx.annotation.Keep
import com.funny.translation.helper.DataSaverUtils
import com.funny.translation.jsBean.core.R
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

@Keep
@kotlinx.serialization.Serializable
enum class Language(val id : Int,var displayText : String = "") {
    AUTO(0),
    CHINESE(1),
    ENGLISH(2),
    JAPANESE(3),
    KOREAN(4),
    FRENCH(5),
    RUSSIAN(6),
    GERMANY(7),
    WENYANWEN(8),
    THAI(9),
    PORTUGUESE(10),
    VIETNAMESE(11),
    ITALIAN(12),
    CHINESE_YUE(13) // 粤语
    ;

    val selectedKey get() = this.name + "_selected"
    val imgSelectedKey get() = this.name + "_img_selected"

}

fun findLanguageById(id : Int) = if(id in allLanguages.indices) {
    allLanguages[id]
} else {
    Language.AUTO
}

val allLanguages = Language.values().asList()
val enabledLanguages = MutableStateFlow(allLanguages.filter { DataSaverUtils.readData(it.selectedKey, true) })

fun initLanguageDisplay(resources : Resources){
    Language.AUTO.displayText = resources.getString(R.string.language_auto)
    Language.CHINESE.displayText = resources.getString(R.string.language_chinese)
    Language.ENGLISH.displayText = resources.getString(R.string.language_english)
    Language.JAPANESE.displayText = resources.getString(R.string.language_japanese)
    Language.KOREAN.displayText = resources.getString(R.string.language_korean)
    Language.FRENCH.displayText = resources.getString(R.string.language_french)
    Language.RUSSIAN.displayText = resources.getString(R.string.language_russian)
    Language.GERMANY.displayText = resources.getString(R.string.language_germany)
    Language.WENYANWEN.displayText = resources.getString(R.string.language_wenyanwen)
    Language.THAI.displayText = resources.getString(R.string.language_thai)
    Language.PORTUGUESE.displayText = resources.getString(R.string.language_portuguese)
    Language.VIETNAMESE.displayText = resources.getString(R.string.language_vietnamese)
    Language.ITALIAN.displayText = resources.getString(R.string.language_italian)
    Language.CHINESE_YUE.displayText = resources.getString(R.string.language_chinese_yue)
}


