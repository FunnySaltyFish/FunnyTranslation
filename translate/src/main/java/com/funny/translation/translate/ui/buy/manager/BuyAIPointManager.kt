package com.funny.translation.translate.ui.buy.manager

import com.funny.translation.network.CommonData
import com.funny.translation.translate.bean.AIPointPlan
import com.funny.translation.translate.network.TransNetwork

class BuyAIPointManager(val name: String): BuyProductManager<AIPointPlan>() {
    private val payService get() = TransNetwork.payService
    override suspend fun callBuyProduct(
        product: AIPointPlan,
        payType: String,
        num: Int
    ): CommonData<BuyProductResponse?> {
        return payService.buyAIPoint(product.plan_name, product.plan_index, payType, num)
    }

    override suspend fun getProducts(): List<AIPointPlan> {
        return payService.getAIPointPlans(name).data ?: emptyList()
    }

    companion object {
        fun find(name: String): BuyAIPointManager {
            return when(name) {
                "ai_text_point" -> BuyAITextPointManager
                "ai_voice_point" -> BuyAIVoicePointManager
                else -> throw Exception("unknown ai point plan name: $name")
            }
        }
        val BuyAITextPointManager = BuyAIPointManager("ai_text_point")
        val BuyAIVoicePointManager = BuyAIPointManager("ai_voice_point")
    }
}
