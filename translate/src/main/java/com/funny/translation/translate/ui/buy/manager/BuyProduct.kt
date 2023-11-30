package com.funny.translation.translate.ui.buy.manager

import com.funny.translation.helper.string
import com.funny.translation.network.CommonData
import com.funny.translation.translate.R
import com.funny.translation.translate.network.TransNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@kotlinx.serialization.Serializable
data class BuyProductResponse(
    val trade_no: String,
    val pay_url: String
)

interface ProductProvider<T> {
    suspend fun getProducts(): List<T>
    suspend fun buyProduct(product: T, payMethodCode: String, num: Int, onReceivePayUrl: (String, String) -> Unit, onPayFinished: (String) -> Unit)
}

abstract class TradeStatusStore {
    internal var currentOrderNo: String? = null
    internal var currentStatus: String? = null
    fun updateOrderStatus(tradeNo: String, status: String) {
        currentStatus = status
    }

    companion object {
        const val STATUS_PAYING = "WAIT_BUYER_PAY"
        const val STATUS_CANCEL_OR_FINISHED = "TRADE_CLOSED"
        const val STATUS_SUCCESS = "TRADE_SUCCESS"
    }
}

abstract class BuyProductManager<T> : TradeStatusStore(), ProductProvider<T> {
    private val vipService get() = TransNetwork.payService
    private var job: Job? = null
    // private val orderStatus = mutableMapOf<String, String>()

    override suspend fun buyProduct(product: T, payMethodCode: String, num: Int, onReceivePayUrl: (String, String) -> Unit, onPayFinished: (String) -> Unit) = withContext(Dispatchers.IO) {
        val resp = callBuyProduct(product, payMethodCode, num)
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

    suspend fun getVipConfigs() = withContext(Dispatchers.IO) {
        vipService.getVipConfigs().data ?: emptyList()
    }
    
    abstract suspend fun callBuyProduct(product: T, payType: String, num: Int): CommonData<BuyProductResponse?>
}