package com.funny.translation.translate.bean

import androidx.annotation.Keep

@Keep
data class UpdateInfo(
    val apk_md5: String?,
    val apk_size: Int?,
    val apk_url: String?,
    val channel: String?,
    val force_update: Boolean?,
    val should_update: Boolean,
    val update_log: String?,
    val version_code: Int?,
    val version_name: String?
)