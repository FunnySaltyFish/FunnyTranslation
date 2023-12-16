package com.funny.translation.helper

object TextSplitter {

    // 适合作为句子结尾的分隔符，比如 。！？及其变种
//    private val punctuationsRegex = Regex("[,，.。！？!?…；;“)）\\\\]】}」』）]")
    // 从上到下，粒度逐渐变细
    private val hierarchyPunctuations = listOf(
        listOf("\n", "\r\n"),
        listOf("。", "！", "？", "…", "；", ";"),
        listOf("”", ")", "）", "]", "】", "}", "」", "』"),
        listOf(",", "，"),
        listOf(" ", "\t", "\r")
    )

    private val allPunctuations = hierarchyPunctuations.flatten()

    /**
     * 尝试按自然语言的规范，分割长度不大于 maxLength 的文本。多余的部分舍弃
     * @param text String
     * @param maxLength Int
     * @param start Int
     */
    fun splitTextNaturally(text: String, maxLength: Int): String {
        if (text.length < maxLength) return text
        // 从后向前处理，以减少计算基础
        val textLength = text.length
        var i = 0 // 上一次处理到哪里了
        var j = textLength - 1 // 结尾的 index
        for (puncs in hierarchyPunctuations) {
            j = textLength - 1
            while (j >= 0) {
                val last = text.rfind(puncs, i, j)
                if (last == -1) {
                    break
                }
                // 找到了
                // 如果长度够了，则直接返回
                if (last <= maxLength) return text.substring(0, last + 1)
                // 否则往前找
                j = last - 1
            }
        }
        if (j == 0)
            return text.substring(0, maxLength)
        return text.substring(0, minOf(j + 1, textLength))
    }

    /**
     * 尝试按自然语言的规范，对文本的最后进行裁剪，尽量保证裁剪后的文本是完整的句子，且长度大于等于 ratio * length
     * @param text String
     * @return String
     */
    fun cutTextNaturally(text: String, ratio: Float = 0.9f): String {
        for(puncs in hierarchyPunctuations) {
            val idx = text.rfind(puncs, 0, text.length - 1)
            if (idx >= text.length * ratio) {
                return text.substring(0, idx + 1)
            }
        }
        return text
    }

    /**
     * 尝试按自然语言的规范，分割长度不大于 maxLength 的文本
     * @param text String
     * @param maxLength Int
     * @param start Int
     */
//    fun splitTextNaturally(text: String, maxLength: Int): String {
//        if (text.length < maxLength) return text
//
//        val separators = listOf("\n", ".", "。", "!", "！", "?", "？", "…", ";", "；", "”", ")", "）", "]", "】", "}", "」", "』", ",", "，")
//        val result = StringBuilder()
//
//        for (separator in separators) {
//            val chunks = text.split(separator, result.length)
//
//            for (chunk in chunks) {
//                if (result.length + chunk.length <= maxLength) {
//                    result.append(chunk + separator)
//
//                    if (result.length >= maxLength) {
//                        return result.toString()
//                    }
//                } else {
//                    return result.toString()
//                }
//            }
//        }
//
//        return result.toString()
//    }

    private fun String.split(separator: String, start: Int): List<String> {
        val result = mutableListOf<String>()
        var last = start
        while (true) {
            val idx = this.indexOf(separator, last)
            if (idx == -1) {
                result.add(this.substring(last))
                break
            } else {
                result.add(this.substring(last, idx + separator.length))
                last = idx + separator.length
            }
        }
        return result
    }

    /**
     * find from right to left, [start, end]
     * @receiver String
     * @param separators List<String>
     * @param start Int
     * @param end Int
     * @return Int
     */
    private fun String.rfind(separators: List<String>, start: Int, end: Int): Int {
        for (i in end downTo start) {
            if (separators.contains(this[i].toString())) {
                return i
            }
        }
        return -1
    }

}