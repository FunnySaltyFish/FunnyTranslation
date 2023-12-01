package com.funny.translation.translate.bean

import com.funny.translation.AppConfig
import com.funny.translation.bean.Price
import com.funny.translation.helper.PriceSerializer
import kotlinx.serialization.Serializable

@kotlinx.serialization.Serializable
data class AIPointPlan(
    val count: Int,
    val plan_name: String,
    val plan_index: Int,
    override val discount: Double,
    @Serializable(with = PriceSerializer::class)
    val vip_price: Price,
    @Serializable(with = PriceSerializer::class)
    override val origin_price: Price
): Product {
    override val id: String = "$plan_name#$plan_index"

    override fun getRealPrice(): Price {
        return if( AppConfig.isVip() ) vip_price else this.origin_price
    }
}

const val AI_TEXT_POINT = "ai_text_point"
const val AI_VOICE_POINT = "ai_voice_point"