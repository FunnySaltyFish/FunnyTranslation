package com.funny.compose.ai.bean

class Prompt(
    val text: String,
    val inputKeys: List<String>,
) {
    fun format(vararg args: Any): String {
        return String.format(text, *args)
    }
}