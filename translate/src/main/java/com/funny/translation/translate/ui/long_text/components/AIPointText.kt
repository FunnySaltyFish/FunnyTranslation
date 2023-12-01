package com.funny.translation.translate.ui.long_text.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.funny.translation.AppConfig
import com.funny.translation.translate.LocalNavController
import com.funny.translation.translate.bean.AI_TEXT_POINT
import com.funny.translation.translate.extentions.formatBraceStyle
import com.funny.translation.translate.navigateSingleTop
import com.funny.translation.translate.ui.TranslateScreen
import com.funny.translation.translate.ui.widget.NumberChangeAnimatedText

@Composable
fun AIPointText(
    planName: String = AI_TEXT_POINT,
) {
    val user = AppConfig.userInfo.value
    val value = if (planName == AI_TEXT_POINT) user.ai_text_point else user.ai_voice_point
    val navController = LocalNavController.current
    NumberChangeAnimatedText(
        modifier = Modifier.clickable {
            navController.navigateSingleTop(
                route = TranslateScreen.BuyAIPointScreen.route.formatBraceStyle(
                    "planName" to planName
                )
            )
        }.padding(8.dp),
        text = value.toString()
    )
}