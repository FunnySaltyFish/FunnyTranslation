package com.funny.translation.helper

// converted from https://github.com/indgov/partial-json-parser/blob/master/partial-json-parser.js
// by ChatGPT
object PartialJsonParser {
    /**
     * 尝试补全不完整的 JSON 字符串
     *
     * examples:
     * ```
     * {"key":"hhh"} -> {"key":"hhh"}
     * {"key":"hhh", "key -> {"key":"hhh"}
     * {"key":"hhh", "key2": "未完成 -> {"key":"hhh", "key2": "未完成"}
     * {"key":"hhh", "key2": [["急急急"]]} -> {"key":"hhh", "key2": [["急急急"]]}
     * {"key":"hhh", "key2": [["急急急", "未完成" -> {"key":"hhh", "key2": [["急急急", "未完成"]]}
     * {"key":"hhh", "key2": [["急急急", -> {"key":"hhh", "key2": [["急急急"]]}
     * {"key":"hhh", "key2": [["急 -> {"key":"hhh", "key2": [["急"]]}
     * {"key":"hhh", "key2": [ -> {"key":"hhh", "key2": []}
     * {"ke -> {}
     * ```
     *
     * 请注意这个方法不一定对所有json有效，而且可能无法正确处理 \" 嵌套的情况
     *
     * @param text String
     * @return String
     */
    fun completePartialJson(text: String): String {
        val tail = mutableListOf<String>()
        var s = text.replace("\r\n", "")
        var oddQuote = false

        for (element in s) {
            when (element) {
                '{' -> tail.add("}")
                '[' -> tail.add("]")
                '"' -> {
                    oddQuote = !oddQuote
                    if (oddQuote) {
                        tail.add("\"")
                    } else {
                        tail.removeAt(tail.lastIndexOf("\""))
                    }
                }
                '}' -> tail.removeAt(tail.lastIndexOf("}"))
                ']' -> tail.removeAt(tail.lastIndexOf("]"))
            }
        }


        if (tail.isNotEmpty()) {
            if (tail.last() == "\"") {
                if (s.last() != ']') {
                    // 从后往前找，如果碰到 "key 这种，把它去掉
                    // 也就是键都没有生成完成
                    var j = s.lastIndex
                    var insideLiteral = s.count { it == '\"' } % 2 == 1
                    var hasEncounteredColon = false
                    while (j > 0) {
                        when (s[j]) {
                            '"' -> {
                                insideLiteral = !insideLiteral
                            }
                            ':' -> {
                                if (!insideLiteral) {
                                    hasEncounteredColon = true
                                }
                            }
                            ',' -> {
                                if (!insideLiteral) {
                                    if (!hasEncounteredColon) {
                                        s = s.slice(0 until j)
                                        tail.removeAt(tail.lastIndexOf("\""))
                                    }
                                    break
                                }
                            }
                            '[' -> {
                                if (!insideLiteral) {
                                    // 向左找，看看是先我碰到 : 还是 ,
                                    // : 例如 {"key":"hhh", "key2": [["急"}
                                    // , 例如 {"text":"to ignite the fire.","keywords":[["Red Guards","红色联合"],["
                                    var k = j - 1
                                    while (k >= 0) {
                                        if (s[k] == ',') {
                                            tail.removeAt(tail.lastIndexOf("]"))
                                            break
                                        } else if (s[k] == ':') {
                                            break
                                        }
                                        k--
                                    }

                                }
                            }
                        }
                        j -= 1
                    }
                    // 如果走到了开头，还是没碰到 :，那么这个时候可能类似 { "key
                    // 直接返回吧
                    if (!hasEncounteredColon && j == 0) {
                        return "{}"
                    }
                }

            } else if (tail.last() == "}") {
                // {"key":"hhh", "key2":
                s = s.trimEnd()
                if (s.last() == ':') {
                    // 从后往前找，如果碰到 "key 这种，把它去掉
                    // 也就是键都没有生成完成
                    var j = s.lastIndex
                    var insideLiteral = false
                    while (j > 0) {
                        when (s[j]) {
                            '"' -> {
                                insideLiteral = !insideLiteral
                            }

                            ',' -> {
                                if (!insideLiteral) {
                                    s = s.slice(0 until j)
                                    break
                                }
                            }
                        }
                        j--
                    }
                }
            }
        }

        // if (tail.isNotEmpty()) {
        //     if (tail[tail.size - 1] == "}") {
        //         if (tempS[tempS.length - 1] != ']') {
        //             var insideLiteral = (tempS.split(".", "\"").size - 1) % 2 == 1
        //             var lastKV = ""
        //             var metAColon = false
        //             var j = tempS.lastIndex
        //             while (j > 0) {
        //                 when (tempS[j]) {
        //                     ':' -> {
        //                         if (!insideLiteral) {
        //                             metAColon = true
        //                             insideLiteral = false
        //                         }
        //                     }

        //                     '{' -> {
        //                         if (!insideLiteral) {
        //                             if (!metAColon) {
        //                                 lastKV = ""
        //                             }
        //                             break
        //                         }
        //                     }

        //                     ',' -> {
        //                         if (!insideLiteral) {
        //                             if (!metAColon) {
        //                                 lastKV = ""
        //                             }
        //                             break
        //                         }
        //                     }

        //                     '"' -> {
        //                         if (!insideLiteral) {
        //                             insideLiteral = true
        //                             lastKV += tempS[j]
        //                         } else {
        //                             lastKV += tempS[j]
        //                             insideLiteral = false
        //                             break
        //                         }
        //                     }

        //                     else -> {
        //                         if (!metAColon) {
        //                             if (j != s.length - 1 || tempS[j] != '}') {
        //                                 lastKV = lastKV + tempS[j]
        //                             }
        //                         }
        //                     }
        //                 }
        //                 j -= 1
        //             }

        //             lastKV = lastKV.reversed()
        //             if (lastKV != "false" && lastKV != "true" && lastKV != "null" &&
        // !lastKV.matches(
        //                     Regex("^\\d+$")
        //                 ) &&
        //                 !(lastKV.length != 1 && lastKV[0] == '"' && lastKV[lastKV.length - 1] ==
        // '"')
        //             ) {
        //                 tempS = tempS.slice(0 until j)
        //                 if (metAColon) {
        //                     val lastKey = lastKV.split(":").first().trim()
        //                     val newKV = "\"$lastKey\": \"未完成\""
        //                     tempS += newKV
        //                 }
        //             }
        //         }
        //     } else if (tail[tail.size - 1] == "]") {
        //         if ((tempS.substring(0, tempS.lastIndexOf("[")).split("\"").size - 1) % 2 == 1) {
        //             tempS = tempS.substring(0, tempS.lastIndexOf("\""))
        //         }
        //     }
        // }

        if (s.isNotEmpty()) {
            val lastCharacter = getNonWhitespaceCharacterOfStringAt(s, s.length - 1)
            if (lastCharacter.first == ',') {
                s = s.slice(0 until lastCharacter.second)
            }
        }

        tail.reverse()

        return s + tail.joinToString(separator = "")
    }

    private fun getNonWhitespaceCharacterOfStringAt(s: String, i: Int): Pair<Char, Int> {
        var index = i
        while (s[index].isWhitespace()) {
            index--
        }
        return s[index] to index
    }
}

//fun main() {
//    val jsons =
//        arrayOf(
////            "{\"key\":\"hhh\"}",
////            "{\"key\":\"hhh\", \"key",
////            "{\"key\":\"hhh\", \"key2\": \"未完成",
////            "{\"key\":\"hhh\", \"key2\": [[\"急急急\"]]}",
////            "{\"key\":\"hhh\", \"key2\": [[\"急急急\", \"未完成\"",
////            "{\"key\":\"hhh\", \"key2\": [[\"急急急\",",
////            "{\"key\":\"hhh\", \"key2\": [[\"急"
//            "{\"key\":\"hhh\", \"key",
//        )
//    for (json in jsons) {
//        val result = PartialJsonParser.completePartialJson(json)
//        println("partial json: \n----origin: $json\n----parsed: $result")
//    }
//}

// kotlinc PartialJsonParser.kt -include-runtime -d PartialJsonParser.jar && java -jar PartialJsonParser.jar