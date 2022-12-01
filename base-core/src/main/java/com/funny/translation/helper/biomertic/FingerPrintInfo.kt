package com.funny.translation.helper.biomertic

import androidx.annotation.Keep

@Keep
data class FingerPrintInfo(
    var encrypted_info: String = "",
    var iv: String = "",
    // 额外信息：
    // 为 "no_user" 代表此用户不存在
    // 为 “new_device#email" 代表此设备是新设备，后半部分为邮箱
    val extra: String = ""
)