package com.funny.translation.trans

import androidx.annotation.Keep

@Keep
data class TranslationResult(
    var engineKind: Short = 0,
    var basicResult: Translation = Translation(""),
    var sourceString: String = "",
    val details: ArrayList<Translation>? = null
) {

    fun setBasicResult(text: String) {
        //Log.d("result[$engineKind][hashCode:${hashCode()}]", "setBasicResult: oldText is ${this.basicResult.trans},  newText is $text")
        basicResult.trans = text

        //该方法调用次数正常
    }
}

@Keep
data class Translation(
    var trans: String,
    var phoneticNotation: String? = null,//注音
    var partOfSpeech: String? = null //词性
)

//艹，就因为这个折腾了一天
val EmptyTranslation = Translation("")
