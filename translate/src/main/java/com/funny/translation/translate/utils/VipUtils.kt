package com.funny.translation.translate.utils

import com.funny.translation.network.CommonData
import com.funny.translation.translate.bean.BuyProductManager
import com.funny.translation.translate.bean.BuyProductResponse
import com.funny.translation.translate.bean.VipConfig
import com.funny.translation.translate.network.TransNetwork


object VipUtils : BuyProductManager<VipConfig>() {
    private val vipService get() = TransNetwork.vipService
    override suspend fun getProducts(): List<VipConfig> {
        return vipService.getVipConfigs().data ?: emptyList()
    }

    override suspend fun callBuyProduct(
        product: VipConfig,
        payType: String,
        num: Int
    ): CommonData<BuyProductResponse?> {
        return vipService.buyVip(product.id, payType, num)
    }
}