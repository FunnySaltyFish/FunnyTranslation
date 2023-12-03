package com.funny.translation.translate

import com.funny.translation.helper.PartialJsonParser
import org.junit.Test

class PartialJsonParserTest {
    @Test
    fun testPartial() {
        val jsons = arrayOf(
            "{\"key\":\"hhh\"}",
            "{\"key\":\"hhh\", \"key2\": ",
            "{\"key\":\"hhh\", \"key",

            "{\"key\":\"hhh\", \"key2\": \"未完成",
            "{\"key\":\"hhh\", \"key2\": [[\"急急急\"]]}",
            "{\"key\":\"hhh\", \"key2\": [[\"急急急\", \"未完成\"",
            "{\"key\":\"hhh\", \"key2\": [[\"急急急\",",
            "{\"key\":\"hhh\", \"key2\": [[\"急",
            "{\"key\":\"hhh\", \"key2\": [",
            "{\"ke",
            "{\"text\":\"to ignite the fire.\",\"keywords\":[[\"Red Guards\",\"红色联合\"],[\""
        )
        for (json in jsons) {
            val result = PartialJsonParser.completePartialJson(json)
//            println("partial json: \n----origin: $json\n----parsed: $result")
            println("$json\n -> \n$result\n")
        }
    }
}