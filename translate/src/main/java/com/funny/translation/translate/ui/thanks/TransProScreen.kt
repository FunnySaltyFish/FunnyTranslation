package com.funny.translation.translate.ui.thanks

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.compose.loading.loadingList
import com.funny.compose.loading.rememberRetryableLoadingState
import com.funny.translation.AppConfig
import com.funny.translation.helper.openUrl
import com.funny.translation.helper.readAssets
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.LocalActivityVM
import com.funny.translation.translate.LocalNavController
import com.funny.translation.translate.R
import com.funny.translation.translate.activity.WebViewActivity
import com.funny.translation.translate.bean.VipConfig
import com.funny.translation.translate.ui.widget.MarkdownText
import com.funny.translation.translate.ui.widget.NumberChangeAnimatedText
import com.funny.translation.translate.ui.widget.TextFlashCanvas
import com.funny.translation.translate.ui.widget.TextFlashCanvasState
import com.funny.translation.translate.utils.VipUtils
import com.funny.translation.ui.touchToScale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

private enum class PayMethod(val iconRes: Int, val titleRes: Int, val code: String) {
    Alipay(R.drawable.ic_alipay, R.string.alipay, "alipay"), Wechat(R.drawable.ic_wechat, R.string.wechat_pay, "wxpay")
}

@Composable
fun TransProScreen(){
    TextFlashCanvas(
        modifier = Modifier.fillMaxSize(),
        state = remember { TextFlashCanvasState() },
        text = "译站专业版",
        textStyle = TextStyle(
            color = Color(0xfffeeb8f),
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        ),
        enterAnimDuration = 2000
    ){
        TransProContent()
    }
}

@Composable
fun TransProContent() {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 12.dp, end = 12.dp, top = 40.dp, bottom = 2.dp)
            .verticalScroll(rememberScrollState())
    ) {
        val (state, retry) = rememberRetryableLoadingState(loader = VipUtils::getVipConfigs)
        val context = LocalContext.current
        val vipTip = rememberSaveable {
            context.readAssets("vip_tip.md").format(1.99f)
        }
        var selectedId by remember {
            mutableStateOf(-1)
        }
        var buyNumber by remember { mutableStateOf(1) }
        var payMethod by remember {
            mutableStateOf(PayMethod.Alipay)
        }
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
        val activityVM = LocalActivityVM.current
        val startBuy = remember {
            lambda@ {
                if (selectedId == -1) {
                    return@lambda
                }
                scope.launch {
                    kotlin.runCatching {
                        VipUtils.buyVip(selectedId, payMethod.code, buyNumber, startPay, onPayFinished = {
                            if (it == "TRADE_SUCCESS") {
                                activityVM.refreshUserInfo()
                                AppConfig.enableVipFeatures()
                                context.toastOnUi("会员购买成功！")
                            }
                            navHostController.popBackStack()
                        })
                    }.onFailure {
                        it.printStackTrace()
                        context.toastOnUi(it.message)
                    }
                }
            }
        }
        BackHandler(enabled = tradeNo != null) {
            VipUtils.updateOrderStatus(tradeNo!!, VipUtils.STATUS_CANCEL_OR_FINISHED)
        }
        LazyRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            loadingList(state, retry, { it.id }) { vipConfig ->
                if (selectedId <= 0) selectedId = vipConfig.id
                VipCard(
                    modifier = Modifier,
                    vipConfig = vipConfig,
                    selectedProvider = { vipConfig.id == selectedId }
                ) {
                    selectedId = vipConfig.id
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))

        PayMethodTile(selected = payMethod == PayMethod.Alipay, payMethod = PayMethod.Alipay) {
            payMethod = it
        }
        Spacer(modifier = Modifier.height(8.dp))
        PayMethodTile(selected = payMethod == PayMethod.Wechat, payMethod = PayMethod.Wechat) {
            payMethod = it
        }
        BuyNumberTile(number = buyNumber, updateNumber = { buyNumber = it })
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    Brush.horizontalGradient(
                        if (selectedId > 0) listOf(Color(0xfffeeb8f), Color(0xfffed837))
                        else listOf(Color.LightGray, Color.LightGray)
                    ),
                    CircleShape
                )
                .clip(CircleShape)
                .clickable(onClick = startBuy),
        ) {
            Text(
                stringResource(id = R.string.buy),
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))
        MarkdownText(markdown = vipTip, color = Color.LightGray, fontSize = 10.sp)
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
        Icon(
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
fun BuyNumberTile(
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
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

@Composable
fun VipCard(
    modifier: Modifier = Modifier,
    vipConfig: VipConfig,
    selectedProvider: () -> Boolean,
    updateSelected: (Boolean) -> Unit
) {
    // 卡片，显示名称，折扣价格（划掉原价），折扣结束时间（时刻倒计时），价格
    val localTextStyle = LocalTextStyle.current
    val selected by rememberUpdatedState(newValue = selectedProvider())
    CompositionLocalProvider(
        LocalTextStyle provides localTextStyle.copy(
            color = if (selected) Color(0xff2177b8) else localTextStyle.color
        )
    ) {
        Box(Modifier.touchToScale { updateSelected(!selected) }) {
            Column(
                modifier
                    .offset(0.dp, 16.dp)
                    .background(Color(0xfff5f9fc), RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        if (selected) Color(0xff8cb6d8) else Color(0xfff5f9fc),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = vipConfig.name,
                    fontSize = 24.sp,
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row {
                    Text(
                        text = "￥${vipConfig.getRealPrice()}",
                        modifier = Modifier
                            .alignByBaseline()
                            .padding(end = 4.dp),
                        fontSize = 40.sp, fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "￥${vipConfig.price}",
                        modifier = Modifier
                            .alignByBaseline(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.LightGray,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(buildAnnotatedString {
                    append("约 ")
                    withStyle(
                        style = MaterialTheme.typography.titleLarge.toSpanStyle()
                            .copy(color = LocalTextStyle.current.color)
                    ) {
                        append(vipConfig.getPricePerDay())
                    }
                    append(" 元/天")
                }, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
            }

            Box(
                modifier = Modifier
                    .background(
                        Color(0xff2277b8),
                        RoundedCornerShape(
                            topStart = 50f,
                            topEnd = 50f,
                            bottomStart = 0f,
                            bottomEnd = 50f
                        )
                    )
                    .padding(4.dp)
            ) {
                if (vipConfig.discount_end_time >= Date())
                    RealTimeCountdown(dueTime = vipConfig.discount_end_time)
                else
                    Text(text = "低价促销", color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 4.dp))
            }
        }
    }
}

@Composable
fun RealTimeCountdown(
    dueTime: Date
) {
    var remainingTime by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            remainingTime =
                ((dueTime.time - System.currentTimeMillis()).milliseconds).toComponents { days, hours, minutes, seconds, _ ->
                    "%d天%02d时%02d分%02d秒".format(days, hours, minutes, seconds)
                }
            delay(1000)
        }
    }

    // 显示为：剩余时间（天，时，分，秒）
    NumberChangeAnimatedText(
        text = remainingTime,
        textSize = 12.sp,
        textColor = Color.White,
        textPadding = PaddingValues(horizontal = (0.5).dp, vertical = 4.dp)
    )
}