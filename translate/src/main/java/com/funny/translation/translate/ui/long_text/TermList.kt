package com.funny.translation.translate.ui.long_text

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf

// 单个术语，源文本：对应翻译
typealias Term = Pair<String, String>

/**
 * TermList，管理 Term，避免出现重复的 Term
 */
@Stable
class TermList {
    private val map by lazy { hashMapOf<String, String>() }
    val list = mutableStateListOf<Term>()

    /**
     * 添加一个术语，如果对应的Key已经存在，则不添加
     * @param term Pair<String, String>
     */
    fun add(term: Term) {
        if (term.first in map) return
        map[term.first] = term.second
        list.add(term)
    }

    fun modify(origin: Term, target: Term) {
        // if (origin.first != target.first) return
//        Log.d(TAG, "modify: $origin -> $target")
//        Log.d(TAG, "before: $this")
        if (target.first in map) return
        map[origin.first] = target.second
        list.remove(origin)
        list.add(target)
//        Log.d(TAG, "after: $this")
    }

    fun remove(term: Term) {
        if (term !in list) return
        map.remove(term.first)
        list.remove(term)
    }

    fun addAll(terms: Collection<Term>) {
        val filtered = terms.asSequence().filter { it.first !in map }.toSet()
        list.addAll(filtered)
        filtered.forEach { map[it.first] = it.second }
    }

    fun addAll(terms: Array<Term>) {
        addAll(terms.toList())
    }

    fun clear() {
        map.clear()
        list.clear()
    }

    fun toList() = list.toList()

    override fun toString() = list.joinToString(separator = ",")

    companion object {
        private const val TAG = "TermList"
    }
}
