package com.funny.translation.translate.ui.buy

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.translation.bean.Price
import com.funny.translation.bean.showWithUnit
import com.funny.translation.translate.R
import com.funny.translation.translate.bean.AIPointPlan
import com.funny.translation.translate.ui.buy.manager.BuyAIPointManager
import com.funny.translation.translate.ui.widget.CommonPage
import com.funny.translation.translate.ui.widget.HintText
import com.funny.translation.ui.FixedSizeIcon
import com.funny.translation.ui.touchToScale
import kotlin.math.roundToInt

@Composable
fun BuyAIPointScreen(
    planName: String = "ai_text_point",
) {
    val manager = BuyAIPointManager.find(planName)
    val titleRes = when (planName) {
        "ai_text_point" -> R.string.buy_ai_text_point
        "ai_voice_point" -> R.string.buy_ai_voice_point
        else -> R.string.buy_ai_point
    }
    CommonPage(title = stringResource(id = titleRes)) {
        BuyProductContent(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            buyProductManager = manager,
            productItem = { product, modifier, selected, updateSelect ->
                AIPointCard(modifier.padding(vertical = 4.dp), product, selected, updateSelect)
            },
            trailingItems = {
                item {
                    HintText(text = stringResource(id = R.string.ai_point_hint))
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun AIPointCard(
    modifier: Modifier = Modifier,
    plan: AIPointPlan,
    selected: Boolean,
    updateSelected: (AIPointPlan) -> Unit
) {
    val animSpec = tween<Color>(500)
    val containerColor by animateColorAsState( if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer, label = "container", animationSpec = animSpec )
    val contentColor by animateColorAsState( if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer, label = "content", animationSpec = animSpec )
    ListItem(
        modifier = modifier
            .touchToScale { updateSelected(plan) }
            .background(containerColor, RoundedCornerShape(percent = 15))
            .padding(horizontal = 8.dp, vertical = 2.dp),
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
                Icons.Filled.Insights,
                modifier = Modifier
                    .size(36.dp)
                    .padding(end = 8.dp),
                contentDescription = null,
            )
        },
        headlineContent = {
            Text(
                text = "%.2f".format(plan.count.toFloat()),
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold
            )
        },
        supportingContent = {
            FlowRow(
                modifier = Modifier.padding(top = 4.dp),
            ) {
                // 估计一下相当于多少 Token
                val token = (plan.count / TOKEN_PER_PRICE * 1000).roundToInt()
                Text(
                    text = buildAnnotatedString {

                        withStyle(style = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold).toSpanStyle()) {
                            append("~ ")
                            append(token.toString())
                        }
                        append(" Tokens")
                    },
                    modifier = Modifier.padding(end = 4.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = LocalContentColor.current.copy(alpha = 0.9f),
                )
            }
        },
        trailingContent = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = 16.dp, top = 2.dp)
            ) {
                BadgedBox(badge = {
                    Badge {
                        Text(text = stringResource(id = R.string.vip_only), fontSize = 8.sp)
                    }
                }) {
                    Text(
                        text = plan.vip_price.showWithUnit(2),
                        modifier = Modifier,
                        fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = plan.origin_price.showWithUnit(2),
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
fun PreviewAIPointCard() {
    AIPointCard(plan = AIPointPlan(
        count = 1,
        origin_price = Price(100),
        vip_price = Price(80),
        plan_name = "ai_text_point",
        plan_index = 0,
        discount = 0.8
    ), selected = false, updateSelected = {})
}

private const val TOKEN_PER_PRICE = 0.016f