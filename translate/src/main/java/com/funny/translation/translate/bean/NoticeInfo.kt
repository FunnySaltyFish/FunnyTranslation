package com.funny.translation.translate.bean

import androidx.annotation.Keep
import java.util.*

@Keep
data class NoticeInfo(val message : String, val date : Date, val url : String?)
