package com.funny.translation.translate.utils

import android.content.Context
import com.funny.translation.helper.readAssets
import com.funny.translation.translate.FunnyApplication.ctx
import org.json.JSONObject
import org.json.JSONException
import java.io.IOException
import java.nio.charset.StandardCharsets

object FunnyEachText {
    var WORDS: JSONObject? = null
    @Throws(IOException::class, JSONException::class)
    fun init(context: Context) {
        val assetsData: String = context.readAssets("words.json")
        WORDS = JSONObject(String(assetsData.toByteArray(), StandardCharsets.UTF_8))
    }

    @get:Throws(IOException::class, JSONException::class)
    val words: JSONObject?
        get() {
            if (WORDS == null) {
                init(ctx)
            }
            return WORDS
        }
}