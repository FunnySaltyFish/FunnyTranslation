package com.funny.translation.helper

class MarkdownTextBuilder(var lineSeparator: String = "  \n") {
    private val lines = arrayListOf<String>()
    private var currentLine: StringBuilder = StringBuilder()
    override fun toString(): String {
        if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
        return lines.joinToString(lineSeparator)
    }

    fun addBold(string: String) {
        currentLine.append(" **$string** ")
    }

    fun addItalian(string: String) {
        currentLine.append(" *$string* ")
    }

    fun addText(string: String) {
        currentLine.append(string)
    }

    fun commitLine() {
        lines.add(currentLine.toString())
        currentLine.clear()
    }

    fun addLink(description: String, url: String) {
        currentLine.append("[$description]($url) ")
    }

}

inline fun buildMarkdown(block: (MarkdownTextBuilder).() -> Unit): String {
    return MarkdownTextBuilder().apply(block).toString()
}