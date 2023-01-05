package com.funny.translation.helper

fun <K, V> MutableMap<K, V>.get(key: K, default: V): V {
    return get(key) ?: default
}