package com.funny.translation.translate.ui.thanks

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.ReadMore
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.AppConfig
import com.funny.translation.bean.UserInfoBean
import com.funny.translation.helper.string
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.LocalActivityVM
import com.funny.translation.translate.R
import com.funny.translation.translate.bean.VipConfig
import com.funny.translation.translate.ui.widget.BuyProductContent
import com.funny.translation.translate.ui.widget.CommonPage
import com.funny.translation.translate.ui.widget.NoticeBar
import com.funny.translation.translate.ui.widget.NumberChangeAnimatedText
import com.funny.translation.translate.ui.widget.TextFlashCanvas
import com.funny.translation.translate.ui.widget.TextFlashCanvasState
import com.funny.translation.translate.utils.VipUtils
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.MarkdownText
import com.funny.translation.ui.touchToScale
import kotlinx.coroutines.delay
import java.util.Date
import kotlin.time.Duration.Companion.milliseconds

private data class VipFeature(
    val icon: ImageVector,
    @StringRes val title:  Int,
    @StringRes val desc: Int,
)

// - 图片翻译每月配额×3
//- 更高的引擎选择数
//- 更多引擎的详细翻译结果
//- 并行翻译（所有引擎同时翻译，大幅提高翻译速度）
//- 主题
private val vipFeatures by lazy {
    arrayOf(
        VipFeature(Icons.Default.AddPhotoAlternate, R.string.more_image_point, R.string.more_image_point_desc),
        VipFeature(Icons.Default.CalendarViewDay, R.string.more_engine, R.string.more_engine_desc),
        VipFeature(Icons.Default.ReadMore, R.string.more_engine_detail, R.string.more_engine_detail_desc),
        VipFeature(Icons.Default.ViewColumn, R.string.parallel_translate, R.string.parallel_translate_desc),
        VipFeature(Icons.Default.Insights, R.string.custom_theme, R.string.custom_theme_desc),
        VipFeature(Icons.Default.Verified, R.string.exclusive_engine, R.string.exclusive_engine_desc),
    )
}

private var showAnim: Boolean = true

@Composable
fun TransProScreen(){
    if (showAnim) {
        TextFlashCanvas(
            modifier = Modifier.fillMaxSize(),
            state = remember { TextFlashCanvasState() },
            text = "译站专业版",
            textStyle = TextStyle(
                color = Color(0xfffeeb8f),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            ),
            enterAnimDuration = 1000,
            showTextDuration = 1000,
            exitAnimDuration = 300
        ) {
            TransProContent()
        }
        DisposableEffect(key1 = Unit) {
            onDispose {
                showAnim = false
            }
        }
    } else {
        TransProContent()
    }
}

@Composable
fun TransProContent() {
    CommonPage(
        Modifier
            .padding(horizontal = 8.dp),
        title = stringResource(id = R.string.trans_pro)
    ) {
        val activityVM = LocalActivityVM.current
        val user = activityVM.userInfo
        val context = LocalContext.current
        BuyProductContent(
            buyProductManager = VipUtils,
            onBuySuccess = {
                AppConfig.enableVipFeatures()
                activityVM.refreshUserInfo()
                context.toastOnUi(R.string.buy_vip_success)
            },
            productItem = { vipConfig, modifier, selected, updateSelect ->
                VipCard(
                    modifier = modifier,
                    vipConfig = vipConfig,
                    selectedProvider = { selected },
                    updateSelected = updateSelect
                )
            },
            leadingItems = {
                item {
                    if (user.isSoonExpire()) {
                        VipExpireTip(user = user)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            },
            trailingItems = {
                item {
                    MarkdownText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 4.dp),
                        markdown = stringResource(R.string.markdown_agree_vip_privacy),
                        fontSize = 12.sp,
                        color = LocalContentColor.current.copy(0.9f),
                        textAlign = TextAlign.Center
                    )
                }
                items(vipFeatures) {
                    ListItem(
                        headlineContent = {
                            Text(text = stringResource(id = it.title))
                        },
                        supportingContent = {
                            Text(text = stringResource(id = it.desc))
                        },
                        leadingContent = {
                            FixedSizeIcon(it.icon, contentDescription = null)
                        }
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VipCard(
    modifier: Modifier = Modifier,
    vipConfig: VipConfig,
    selectedProvider: () -> Boolean,
    updateSelected: (VipConfig) -> Unit
) {
    // 卡片，显示名称，折扣价格（划掉原价），折扣结束时间（时刻倒计时），价格
    val selected by rememberUpdatedState(newValue = selectedProvider())
    val animSpec = tween<Color>(500)
    val containerColor by animateColorAsState( if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer, label = "container", animationSpec = animSpec )
    val contentColor by animateColorAsState( if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer, label = "content", animationSpec = animSpec )
    ListItem(
        modifier = modifier
            .touchToScale { updateSelected(vipConfig) }
            .background(containerColor, RoundedCornerShape(percent = 30))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        colors = ListItemDefaults.colors(
            containerColor = Color.Unspecified,
            leadingIconColor = contentColor,
            headlineColor = contentColor,
            overlineColor = contentColor,
            supportingColor = contentColor,
            trailingIconColor = contentColor
        ),
        leadingContent = {
            FixedSizeIcon(
                Icons.Filled.Verified,
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 8.dp),
                contentDescription = null,
            )
        },
        headlineContent = {
            Text(
                text = vipConfig.name,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold
            )
        },
        supportingContent = {
            FlowRow {
                if (vipConfig.discount_end_time >= Date())
                    RealTimeCountdown(dueTime = vipConfig.discount_end_time)
                else
                    Text(
                        text = stringResource(R.string.low_price_promotion),
                    )
                Text(text = " | ")
                Text(buildAnnotatedString {
                    append(stringResource(R.string.nearly))
                    withStyle(
                        style = LocalTextStyle.current.toSpanStyle()
                            .copy(fontWeight = FontWeight.W500)
                    ) {
                        append(vipConfig.getPricePerDay())
                    }
                    append(stringResource(R.string.price_per_day))
                })
            }
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "￥${vipConfig.getRealPriceStr()}",
                    modifier = Modifier,
                    fontSize = 18.sp, fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "￥${vipConfig.price}",
                    modifier = Modifier,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = LocalContentColor.current.copy(alpha = 0.9f),
                    textDecoration = TextDecoration.LineThrough
                )
            }
        }
    )
}

@Preview
@Composable
private fun PreviewVipCard() {
    var selected by remember { mutableStateOf(false) }
    VipCard(vipConfig = VipConfig(
        id = 1,
        name = "月卡",
        price = 199.0,
        discount_end_time = Date(),
        duration = 30.0,
        discount = 0.8,
        level = 1
    ), selectedProvider = { selected }, updateSelected = { selected = !selected })
}

@Composable
private fun RealTimeCountdown(
    dueTime: Date
) {
    var remainingTime by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            remainingTime =
                ((dueTime.time - System.currentTimeMillis()).milliseconds).toComponents { days, hours, minutes, seconds, _ ->
                    string(R.string.day_hour_min_sec).format(days, hours, minutes, seconds)
                }
            delay(1000)
        }
    }

    // 显示为：剩余时间（天，时，分，秒）
    NumberChangeAnimatedText(
        text = remainingTime,
        textSize = 12.sp,
        textColor = Color.White,
    )
}

@Composable
private fun VipExpireTip(user: UserInfoBean) {
    NoticeBar(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        text = stringResource(R.string.vip_soon_expire_tip, user.vipEndTimeStr()),
        singleLine = true,
        showClose = true,
    )
}