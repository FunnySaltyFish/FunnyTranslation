package com.funny.translation.translate.bean

import androidx.compose.runtime.Stable
import com.funny.translation.bean.Price
import com.funny.translation.bean.show
import com.funny.translation.helper.DateSerializerType1
import com.funny.translation.helper.PriceSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
    override val id : Int,
    val level : Int,
    @Serializable(with = PriceSerializer::class)
    @SerialName("price")
    override val origin_price : Price,
    override val discount : Double,
    val duration : Double,
    val name : String,
    @kotlinx.serialization.Serializable(with = DateSerializerType1::class)
    val discount_end_time : Date
): Product {

    fun getPricePerDay() : String {
        // 保留三位小数
        return (getRealPrice() / duration.toBigDecimal()).show(3)
    }
}
