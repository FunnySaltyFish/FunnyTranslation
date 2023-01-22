package com.funny.translation.translate.bean

import androidx.annotation.Keep
import com.funny.translation.helper.DateSerializerType1
import kotlinx.serialization.Serializable
import java.util.*

@Keep
@Serializable
data class NoticeInfo(val message : String, @Serializable(with = DateSerializerType1::class) val date : Date, val url : String?)
