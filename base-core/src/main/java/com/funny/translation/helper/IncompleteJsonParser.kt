package com.funny.translation.helper

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 *
 * @property currentText String
 * @property currentIdx Int
 * @property result HashMap<String, Any?>
 * @constructor
 */
class IncompleteJsonParser(val keysWithType: Map<String, Class<*>>) {
    // 当前已经解析完的
    var currentText: String = ""
    // 当前解析到的 idx
    var currentIdx: Int = 0
    // 当前解析到的 key 和 value 对
    val result = hashMapOf<String, Any?>()

    suspend fun update(newPart: String) {
        currentText += newPart
        val json = JsonX.formatter
        var jsonString = currentText.removePrefix("{")
        when {
            jsonString[0] == '"' && jsonString[jsonString.lastIndex] != '"' -> jsonString += '"'
        }

        try {
            val parsedJson = json.decodeFromString<JsonObject>("{$jsonString}")
            parsedJson.forEach { (key, jsonElement) ->
                val value = parseJsonValue(jsonElement, keysWithType[key])
                result[key] = value
            }

            currentIdx = currentText.length
        } catch (e: Exception) {
            // 处理异常
        }
    }

    private fun parseJsonValue(jsonElement: JsonElement, type: Class<*>?): Any? {
        return when {
            jsonElement.jsonPrimitive.isString -> jsonElement.jsonPrimitive.content
            jsonElement.jsonPrimitive.booleanOrNull != null -> jsonElement.jsonPrimitive.boolean
            jsonElement is JsonObject -> {
                val nestedResult = hashMapOf<String, Any?>()
                jsonElement.forEach { (key, nestedJsonElement) ->
                    val nestedValue = parseJsonValue(nestedJsonElement, null)
                    nestedResult[key] = nestedValue
                }
                nestedResult
            }
            else -> null
        }
    }
}