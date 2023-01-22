package com.funny.translation.network

import androidx.annotation.Keep

@Keep
@kotlinx.serialization.Serializable
data class CommonData<T>(val code: Int, val message: String? = null, val data: T? = null, val error_msg:String? = null)
