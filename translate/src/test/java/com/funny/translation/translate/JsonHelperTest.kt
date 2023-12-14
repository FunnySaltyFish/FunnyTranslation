package com.funny.translation.translate

import com.funny.translation.translate.ui.long_text.helper.JsonHelper
import org.junit.Test

class JsonHelperTest {
    @Test
    fun test_jsonHelper() {
        val incorrectText = "{\"text\": \"It says: \\\"Nice to meet you\\\" \"Hello World\"\" }"
        val jsonHelper = JsonHelper()
        for (c in incorrectText) {
            jsonHelper.update(c.toString())
        }
        println(incorrectText)
        println(jsonHelper.toString())
    }
}