package com.funny.translation.translate.utils

import com.funny.translation.translate.bean.VipConfig
import com.funny.translation.translate.network.TransNetwork
import kotlinx.coroutines.*
import kotlinx.serialization.json.jsonPrimitive

object VipUtils {
    private val vipService get() = TransNetwork.vipService
    private var currentOrderNo: String? = null
    private var currentStatus: String? = null
    private var job: Job? = null
    // private val orderStatus = mutableMapOf<String, String>()

    fun updateOrderStatus(tradeNo: String, status: String){
        currentStatus = status
    }

    private const val STATUS_PAYING = "WAIT_BUYER_PAY"
    const val STATUS_CANCEL_OR_FINISHED = "TRADE_CLOSED"

    suspend fun getVipConfigs() = withContext(Dispatchers.IO){
        vipService.getVipConfigs().data ?: emptyList()
    }

    suspend fun buyVip(vipConfigId: Int, payMethodCode: String, num: Int, onReceivePayUrl: (String, String) -> Unit, onPayFinished: (String) -> Unit) = withContext(Dispatchers.IO){
        val resp = vipService.buyVip(vipConfigId, payMethodCode, num)
        val obj = resp.data ?: throw Exception("发起支付失败！" + resp.message)
        val tradeNo = obj.trade_no
        val payUrl = obj.pay_url
        onReceivePayUrl(tradeNo, payUrl)
        currentStatus = STATUS_PAYING
        currentOrderNo = tradeNo

        job?.cancel()
        job = launch {
            while (currentStatus == STATUS_PAYING) {
                delay(1000)
                val queryResp = vipService.queryOrderStatus(tradeNo)
                val status = queryResp.data ?: throw Exception("查询订单状态失败！" + queryResp.message)
                if (status != "paying") {
                    withContext(Dispatchers.Main) {
                        onPayFinished(status)
                    }
                    break
                }
            }
        }
    }
}