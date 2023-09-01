package com.funny.translation.bean

import com.funny.translation.BaseApplication
import com.funny.translation.core.R
import java.util.Locale

enum class AppLanguage(private val descriptionId: Int) {
    FOLLOW_SYSTEM(R.string.follow_system),
    ENGLISH(R.string.language_english),
    CHINESE(R.string.language_chinese);

    val description = BaseApplication.ctx.getString(descriptionId)

    fun toLocale(): Locale = when (this) {
        FOLLOW_SYSTEM -> Locale.getDefault()
        ENGLISH -> Locale.ENGLISH
        CHINESE -> Locale.CHINESE
    }

    override fun toString(): String {
        return BaseApplication.ctx.resources.getString(descriptionId)
    }

}