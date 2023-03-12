package com.funny.translation.translate.network.service

import com.funny.translation.helper.JwtTokenRequired
import com.funny.translation.network.CommonData
import com.funny.translation.translate.bean.VipConfig
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

@kotlinx.serialization.Serializable
data class BuyVipResponse(
    val trade_no: String,
    val pay_url: String
)

interface VipService {
    @GET("pay/get_vip_configs")
    suspend fun getVipConfigs() : CommonData<List<VipConfig>>

    @JwtTokenRequired
    @POST("pay/buy_vip")
    @FormUrlEncoded
    suspend fun buyVip(
        @Field("id") vipConfigId: Int,
        @Field("type") payMethodCode: String = "alipay",
        @Field("number") number: Int = 1
    ) : CommonData<BuyVipResponse?>


    @POST("pay/query_order_status")
    @FormUrlEncoded
    suspend fun queryOrderStatus(
        @Field("trade_no") tradeNo: String
    ) : CommonData<String?>
}