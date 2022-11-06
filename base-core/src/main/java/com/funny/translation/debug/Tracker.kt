package com.funny.translation.debug

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Maybe it is useful, but I don't know how to use it. I just write it.
 * @property data HashMap<String, Any>
 * @constructor
 */
class Tracker(val data: HashMap<String, Any>) {
    fun put(item: Pair<String, Any>) {
        data[item.first] = item.second
    }

    operator fun set(key: String, value: Any) {
        data[key] = value
    }

    override fun toString(): String =
        buildString {
            append("[")
            for (entry in data){
                append(entry.key)
                append("=")
                append(entry.value)
                append(", ")
            }
            append("]")
        }
}

val LocalTracker = staticCompositionLocalOf {
    Tracker(hashMapOf())
}

object TrackerKeys {
    const val SCREEN = "screen"
    const val COMPOSABLE = "composable"
    const val ACTION = "action"
}
