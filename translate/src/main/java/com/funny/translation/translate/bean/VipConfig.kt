package com.funny.translation.translate.bean

import androidx.compose.runtime.Stable
import com.funny.translation.helper.DateSerializerType1
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Stable
@kotlinx.serialization.Serializable
/**
 * {
        "id": 1,
        "level": 1,
        "price": 1.99,
        "discount": 0.5,
        "duration": 30,
        "name": "月卡",
        "discount_end_time": "2023-03-01 00:00:00"
   }
 */
data class VipConfig(
    val id : Int,
    val level : Int,
    val price : Double,
    val discount : Double,
    val duration : Double,
    val name : String,
    @kotlinx.serialization.Serializable(with = DateSerializerType1::class)
    val discount_end_time : Date
){
    fun getRealPrice() : String {
        // 保留两位小数，向下取
        val p = (price * discount).toBigDecimal().setScale(2, BigDecimal.ROUND_DOWN)
        return p.toString()
    }

    fun getPricePerDay() : String {
        // 保留三位小数
        return "%.3f".format(Locale.getDefault(),price * discount / duration)
    }
}
