package com.funny.translation.bean

import com.funny.translation.translate.Language

data class TranslationConfig(
    var sourceString: String? = null,
    var sourceLanguage: Language? = null,
    var targetLanguage: Language? = null
){
    fun clear(){
        sourceLanguage = null
        sourceString = null
        targetLanguage = null
    }

    fun isValid(): Boolean {
        return sourceString?.isNotEmpty() == true && sourceLanguage != null && targetLanguage != null
    }
}
