package com.funny.translation.js

import com.funny.translation.bean.Consts
import com.funny.translation.bean.LanguageBean
import com.funny.translation.js.bean.JsBean


object JSUtils {
    fun coverJSToLanguageBean(jsList: ArrayList<JsBean>): ArrayList<LanguageBean> {
        val result: ArrayList<LanguageBean> = ArrayList()
        for ((i, js) in jsList.withIndex()) {
            val bean = JSLanguageBean(js.id)
            bean.text = js.fileName
            bean.userData = i.toShort()
            bean.checkKind = Consts.CHECK_MULTI
            result.add(bean)
        }
        return result
    }
}