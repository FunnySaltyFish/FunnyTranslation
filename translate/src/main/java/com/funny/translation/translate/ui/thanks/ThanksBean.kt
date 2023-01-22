package com.funny.translation.translate.ui.thanks

import com.funny.translation.helper.DateSerializerType1
import kotlinx.serialization.Serializable
import java.util.*

@kotlinx.serialization.Serializable
data class Sponsor(
    val name : String,
    val message : String? = null,
    @Serializable(with = DateSerializerType1::class) val date : Date,
    val money : Int
){
    val key get() = "$name:${date.time}:${money}"
}