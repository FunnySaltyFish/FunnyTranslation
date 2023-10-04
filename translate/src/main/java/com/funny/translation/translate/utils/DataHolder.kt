package com.funny.translation.translate.utils

import java.util.WeakHashMap

object DataHolder {
    private val map by lazy {
        WeakHashMap<String, Any?>()
    }

    fun put(key: String, value: Any?) {
        map[key] = value
    }

    fun <T> get(key: String): T? {
        return map[key] as? T
    }
}