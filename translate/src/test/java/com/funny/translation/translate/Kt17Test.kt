package com.funny.translation.translate

interface Bar {
    fun foo() = "foo"
}

class BarWrapper(val bar: Bar): Bar by bar

fun main() {
    val bw = BarWrapper(object: Bar {})
    println(bw.foo())
}