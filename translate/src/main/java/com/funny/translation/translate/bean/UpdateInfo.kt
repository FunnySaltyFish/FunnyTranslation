package com.funny.translation.translate.bean

import androidx.annotation.Keep

@Keep
@kotlinx.serialization.Serializable
data class UpdateInfo(
    val apk_md5: String? = null,
    val apk_size: Int? = null,
    val apk_url: String? = null,
    val channel: String? = null,
    val force_update: Boolean? = null,
    val should_update: Boolean = false,
    val update_log: String? = null,
    val version_code: Int? = null,
    val version_name: String? = null
)