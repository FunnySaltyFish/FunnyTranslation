package com.funny.translation.translate

import com.funny.translation.helper.PartialJsonParser
import org.junit.Test

class PartialJsonParserTest {
    @Test
    fun testPartial() {
        val jsons = arrayOf(
            "{\"key\":\"hhh\"}",
            "{\"key\":\"hhh\", \"key",
            "{\"key\":\"hhh\", \"key2\": \"未完成",
            "{\"key\":\"hhh\", \"key2\": [[\"急急急\"]]}",
            "{\"key\":\"hhh\", \"key2\": [[\"急急急\", \"未完成\"",
            "{\"key\":\"hhh\", \"key2\": [[\"急急急\",",
            "{\"key\":\"hhh\", \"key2\": [[\"急",
            "{\"key\":\"hhh\", \"key2\": [",
            "{\"ke"
        )
        for (json in jsons) {
            val result = PartialJsonParser.completePartialJson(json)
//            println("partial json: \n----origin: $json\n----parsed: $result")
            println("$json -> $result")
        }
    }
}