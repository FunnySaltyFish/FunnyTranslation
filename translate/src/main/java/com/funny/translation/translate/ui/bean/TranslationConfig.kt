package com.funny.translation.translate.ui.bean

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
}
