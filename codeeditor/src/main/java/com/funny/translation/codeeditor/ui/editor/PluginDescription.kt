package com.funny.translation.codeeditor.ui.editor

import com.funny.translation.js.core.JsInterface
import com.funny.translation.translate.Language
import io.github.rosemoe.editor.langs.desc.JavaScriptDescription

val FunnyPluginDescription = PluginDescription()

class PluginDescription : JavaScriptDescription() {

    override fun getKeywords(): Array<String> {
        val originKeywords = super.getKeywords().toMutableList()
        for (language in Language.values()){
            originKeywords.add("LANGUAGE_${language.name}")
        }
        originKeywords.add("funny")
        originKeywords.add("http")
        originKeywords.add("BASE_URL")
        val clazz = JsInterface::class.java
        val set = mutableSetOf<String>()
        clazz.declaredMethods.forEach {
            set.add(it.name)
        }
        originKeywords.addAll(set)
        return originKeywords.toTypedArray()
    }
}