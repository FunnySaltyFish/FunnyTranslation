package com.funny.translation.translate.ui.buy.manager

import com.funny.translation.network.CommonData
import com.funny.translation.translate.bean.VipConfig
import com.funny.translation.translate.network.TransNetwork


object BuyVIPManager : BuyProductManager<VipConfig>() {
    private val vipService get() = TransNetwork.payService
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