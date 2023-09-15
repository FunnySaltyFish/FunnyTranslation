package com.funny.translation.translate.utils

import com.funny.translation.helper.string
import com.funny.translation.translate.R
import com.funny.translation.translate.network.TransNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        val obj = resp.data ?: throw Exception(string(R.string.failed_to_start_pay) + resp.message)
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
                val status = queryResp.data ?: throw Exception(string(R.string.failed_to_query_order) + queryResp.message)
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