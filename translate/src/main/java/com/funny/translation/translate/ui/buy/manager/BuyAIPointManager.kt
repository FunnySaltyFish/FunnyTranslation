package com.funny.translation.translate.ui.buy.manager

import com.funny.translation.AppConfig
import com.funny.translation.network.CommonData
import com.funny.translation.translate.bean.AIPointPlan
import com.funny.translation.translate.bean.AI_TEXT_POINT
import com.funny.translation.translate.bean.AI_VOICE_POINT
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

    fun addToUser(num: Int) {
        val user = AppConfig.userInfo.value
        if (name == AI_TEXT_POINT) {
            AppConfig.userInfo.value = user.copy(ai_text_point = user.ai_text_point + num.toBigDecimal())
        } else if (name == AI_VOICE_POINT) {
            AppConfig.userInfo.value = user.copy(ai_voice_point = user.ai_voice_point + num.toBigDecimal())
        }
    }

    companion object {
        fun find(name: String): BuyAIPointManager {
            return when(name) {
                AI_TEXT_POINT -> BuyAITextPointManager
                AI_VOICE_POINT -> BuyAIVoicePointManager
                else -> throw Exception("unknown ai point plan name: $name")
            }
        }

        private val BuyAITextPointManager = BuyAIPointManager(AI_TEXT_POINT)
        private val BuyAIVoicePointManager = BuyAIPointManager(AI_VOICE_POINT)
    }
}
