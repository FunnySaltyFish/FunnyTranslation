package com.funny.translation.translate.ui.buy

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.funny.compose.loading.loadingList
import com.funny.compose.loading.rememberRetryableLoadingState
import com.funny.translation.WebViewActivity
import com.funny.translation.helper.openUrl
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.LocalNavController
import com.funny.translation.translate.R
import com.funny.translation.translate.bean.Product
import com.funny.translation.translate.ui.buy.manager.BuyProductManager
import com.funny.translation.translate.ui.buy.manager.TradeStatusStore.Companion.STATUS_CANCEL_OR_FINISHED
import com.funny.translation.translate.ui.widget.NavPaddingItem
import com.funny.translation.translate.ui.widget.NumberChangeAnimatedText
import com.funny.translation.ui.FixedSizeIcon
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.math.BigDecimal
import java.math.RoundingMode

private enum class PayMethod(val iconRes: Int, val titleRes: Int, val code: String) {
    Alipay(R.drawable.ic_alipay, R.string.alipay, "alipay"), Wechat(R.drawable.ic_wechat, R.string.wechat_pay, "wxpay")
}

@Composable
fun <T: Product> BuyProductContent(
    contentPadding: PaddingValues = PaddingValues(8.dp),
    buyProductManager: BuyProductManager<T>,
    onBuySuccess: () -> Unit = {},
    productItem: @Composable (product: T, modifier: Modifier, selected: Boolean, updateSelect: (T) -> Unit) -> Unit,
    leadingItems: LazyListScope.() -> Unit = {},
    trailingItems: LazyListScope.() -> Unit = {},
) {
    val (state, retry) = rememberRetryableLoadingState(loader = buyProductManager::getProducts)
    val context = LocalContext.current
    var selectedVipConfig: T? by remember { mutableStateOf(null) }
    var buyNumber by remember { mutableIntStateOf(1) }
    var payMethod by remember { mutableStateOf(PayMethod.Alipay) }
    val scope = rememberCoroutineScope()
    var tradeNo: String? = remember { null }
    val navHostController = LocalNavController.current
    val startPay = remember {
        lambda@ { t: String,  payUrl: String ->
            tradeNo = t
            if (payUrl.startsWith("http"))
                WebViewActivity.start(context, payUrl)
            else context.openUrl(payUrl)
        }
    }
    val startBuy = remember {
        lambda@ {
            selectedVipConfig ?: return@lambda
            scope.launch {
                kotlin.runCatching {
                    buyProductManager.buyProduct(selectedVipConfig!!, payMethod.code, buyNumber, startPay, onPayFinished = {
                        if (it == "TRADE_SUCCESS") {
                            onBuySuccess()
                        }
                        navHostController.popBackStack()
                    })
                }.onFailure {
                    it.printStackTrace()
                    if (it is HttpException && it.code() == 401) return@onFailure
                    context.toastOnUi(it.message)
                }
            }
        }
    }
    BackHandler(enabled = tradeNo != null) {
        buyProductManager.updateOrderStatus(tradeNo!!, STATUS_CANCEL_OR_FINISHED)
    }
    LazyColumn(
        Modifier.fillMaxWidth(),
        contentPadding = contentPadding,
    ) {
        leadingItems()
        loadingList(state, retry, { it.id }) { vipConfig ->
            if (selectedVipConfig == null) selectedVipConfig = vipConfig
            productItem(
                vipConfig,
                Modifier,
                vipConfig.id == selectedVipConfig?.id
            ) {
                selectedVipConfig = vipConfig
            }
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            PayMethodTile(selected = payMethod == PayMethod.Alipay, payMethod = PayMethod.Alipay) {
                payMethod = it
            }
        }
        item {
            PayMethodTile(selected = payMethod == PayMethod.Wechat, payMethod = PayMethod.Wechat) {
                payMethod = it
            }
        }
        item {
            BuyNumberTile(number = buyNumber, updateNumber = { buyNumber = it })
        }
        item {
            Button(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                onClick = startBuy,
            ) {
                FixedSizeIcon(Icons.Default.ShoppingCart, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                NumberChangeAnimatedText(
                    text = selectedVipConfig?.getRealPrice()?.times(
                        BigDecimal(buyNumber)
                    )?.setScale(2, RoundingMode.HALF_UP)?.toString() ?: "0.0",
                    textColor = LocalContentColor.current,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(id = R.string.buy),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        trailingItems()
        item {
            NavPaddingItem()
        }
    }
}

@Composable
private fun PayMethodTile(
    selected: Boolean,
    payMethod: PayMethod,
    updatePayMethod: (PayMethod) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { updatePayMethod(payMethod) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        FixedSizeIcon(
            modifier = Modifier.size(48.dp),
            painter = painterResource(id = payMethod.iconRes),
            contentDescription = stringResource(id = payMethod.titleRes),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.width(24.dp))
        Text(stringResource(id = payMethod.titleRes))
        Spacer(modifier = Modifier.weight(1f))
        RadioButton(
            selected = selected,
            onClick = null
        )
    }
}

@Composable
private fun BuyNumberTile(
    number: Int,
    updateNumber: (Int) -> Unit
) {
    // 购买数量 - 1 +
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = 16.dp, end = 0.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(id = R.string.buy_number))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { if (number > 1) updateNumber(number - 1) }) {
                Text(text = "-")
            }
            NumberChangeAnimatedText(text = number.toString())
            TextButton(onClick = { updateNumber(number + 1) }) {
                Text(text = "+")
            }
        }
    }
}