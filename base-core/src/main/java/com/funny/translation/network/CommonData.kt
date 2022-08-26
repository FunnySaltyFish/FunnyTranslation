package com.funny.translation.network

import androidx.annotation.Keep

@Keep
data class CommonData<T>(val code: Int, val message: String, val data: T?, val error_msg:String?)
