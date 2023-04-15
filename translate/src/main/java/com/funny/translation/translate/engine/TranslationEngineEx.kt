package com.funny.translation.translate.engine

import com.funny.translation.translate.TranslationEngine

val TranslationEngine.selectKey
    get() = if (this is ImageTranslationEngine) this.name + "_IMG_SELECTED" else this.name + "_SELECTED"