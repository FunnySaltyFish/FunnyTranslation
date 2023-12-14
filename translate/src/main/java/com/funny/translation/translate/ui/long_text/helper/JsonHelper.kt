package com.funny.translation.translate.ui.long_text.helper

/**
 * 逐步存储流式输出的 JSON，并且处理一下嵌套 " 错误的问题，比如：
 * {"text": "It says: "Hello World"" } ->
 * {"text": "It says: \"Hello World\"" }
 */
class JsonHelper() {
    private val sb = StringBuilder()
    // 当前这一段是否在 text 这个值的范围内
    private var inText: Boolean = false

    fun update(part: String) {
        val cur = sb.toString()

        if (cur.length <= LEN_PREFIX) {
            sb.append(part)
            return
        }

        // 后面有概率出现重复的 "，这里做一下处理
        val s = sb.last() + part
        // 如果出现了 " 且不是 \"
        var i = s.lastIndex
        while (i > 0) {
            if (s[i] == '"' && s[i-1] != '\\') {
                // 手动加上反斜杠
                sb.deleteCharAt(sb.lastIndex)
                sb.append(s.substring(0, i) + "\\" + s.substring(i))
                return
            }
            i--
        }

        sb.append(part)
    }

    fun clear() {
        sb.clear()
    }

    override fun toString() = sb.toString()

    companion object {
        private const val LEN_PREFIX = "{\"text\":\"".length
    }
}