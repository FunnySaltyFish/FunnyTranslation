package com.funny.translation.translate.bean

import androidx.compose.runtime.Stable
import com.funny.translation.helper.DateSerializerType1
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date

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
    fun getRealPrice() = BigDecimal(price) * BigDecimal(discount)

    // 保留两位小数，向下取
    fun getRealPriceStr() = getRealPrice().setScale(2, RoundingMode.HALF_UP).toString()

    fun getPricePerDay() : String {
        // 保留三位小数
        return (getRealPrice() / duration.toBigDecimal()).setScale(3, RoundingMode.HALF_UP).toPlainString()
    }
}
