package com.funny.translation.translate.ui.long_text.bean

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import com.funny.translation.helper.string
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.R
import com.funny.translation.translate.appCtx
import com.funny.translation.translate.ui.long_text.Term

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
    fun add(term: Term, alert: Boolean = false) {
        if (term.first in map) {
            if (alert) appCtx.toastOnUi(string(R.string.existed_term_tip, term.first))
            return
        }
        map[term.first] = term.second
        list.add(term)
    }

    fun modify(origin: Term, target: Term, alert: Boolean = false) {
        if (target.first !in map) { // 不在就删掉原本的，新增一个
            map.remove(origin.first)
            map[target.first] = target.second
            list.remove(origin)
            list.add(target)
        } else { // 在的话就更新
            map[target.first] = target.second
            list.remove(origin)
            list.add(target)
        }
    }

    fun upsert(term: Term) {
        val old = map[term.first]
        if (old == null) { // 不在，则新增
            map[term.first] = term.second
            list.add(term)
        } else { // 更新
            map[term.first] = term.second
            list.remove(term.first to old)
            list.add(term)
        }
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