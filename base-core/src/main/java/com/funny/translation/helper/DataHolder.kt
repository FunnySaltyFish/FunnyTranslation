package com.funny.translation.helper

import java.lang.ref.WeakReference

object DataHolder {
    private val map by lazy {
        HashMap<String, WeakReference<Any?>>()
    }

    fun put(key: String, value: Any?) {
        map[key] = WeakReference(value)
    }

    fun <T> get(key: String): T? {
        return map[key]?.get() as? T
    }

    fun remove(key: String) {
        map.remove(key)
    }

    fun contains(key: String): Boolean {
        return map.containsKey(key)
    }
}