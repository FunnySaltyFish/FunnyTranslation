package com.funny.translation.translate

class LocalFunctionTest {
    // 旧的
    fun foo() {
        var invalidated: HashSet<Any>? = null
        fun bar() {
            invalidated = HashSet()
        }
        bar()
        invalidated?.add("1")
    }

    // 新的
    fun foo2(value: Any) {
        var invalidated: HashSet<Any>? = null
        invalidated = invalidated?.bar2(value)
    }

    private fun HashSet<Any>.bar2(value: Any): HashSet<Any> {
        val set = this
        set.add(value)
        return set
    }
}