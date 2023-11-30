package com.funny.translation.translate.network.service

import com.funny.translation.helper.JwtTokenRequired
import com.funny.translation.network.CommonData
import com.funny.translation.translate.bean.AIPointPlan
import com.funny.translation.translate.bean.VipConfig
import com.funny.translation.translate.ui.buy.manager.BuyProductResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface PayService {
    @GET("pay/get_vip_configs")
    suspend fun getVipConfigs() : CommonData<List<VipConfig>>

    @JwtTokenRequired
    @POST("pay/buy_vip")
    @FormUrlEncoded
    suspend fun buyVip(
        @Field("id") vipConfigId: Int,
        @Field("type") payMethodCode: String = "alipay",
        @Field("number") number: Int = 1
    ) : CommonData<BuyProductResponse?>


    @POST("pay/query_order_status")
    @FormUrlEncoded
    suspend fun queryOrderStatus(
        @Field("trade_no") tradeNo: String
    ) : CommonData<String?>

    // get_ai_point_plans, get, plan_name: string
    @GET("pay/get_ai_point_plans")
    suspend fun getAIPointPlans(
        @Query("plan_name") planName: String
    ) : CommonData<List<AIPointPlan>>

    /*
    @bp_pay.route("/buy_ai_point", methods=["POST"])
@get_user_from_jwt
async def buy_ai_point():
    form = await request.form
    plan_name = form.get("plan_name")
    plan_index = form.get("plan_index", 0, type=int)
    pay_type = form.get("pay_type", "alipay")
    number = int(form.get("number", 1))
     */
    @POST("pay/buy_ai_point")
    @FormUrlEncoded
    suspend fun buyAIPoint(
        @Field("plan_name") planName: String,
        @Field("plan_index") planIndex: Int,
        @Field("pay_type") payType: String = "alipay",
        @Field("number") number: Int = 1
    ) : CommonData<BuyProductResponse?>
}