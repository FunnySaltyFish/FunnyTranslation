package com.funny.jetsetting.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// https://github.com/re-ovo/compose-setting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingBaseItem(
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    title: @Composable () -> Unit,
    text: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    val throttleHandler = rememberSaveable(300, saver = ThrottleHandler.Saver) { ThrottleHandler(1000) }
    Surface(
        onClick = {
            throttleHandler.process(onClick)
        },
        color = Color.Unspecified,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = modifier
                .padding(
                    horizontal = MenuTokens.ContentPaddingHorizontal,
                    vertical = MenuTokens.ContentPaddingVertical
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MenuTokens.ElementHorizontalSpace)
        ) {
            icon?.invoke()
            Column(
                modifier = Modifier.weight(1f)
            ) {
                ProvideTextStyle(
                    MaterialTheme.typography.titleLarge.copy(
                        fontSize = 16.sp, fontWeight = FontWeight.W500
                    )
                ) {
                    title()
                }
                ProvideTextStyle(MaterialTheme.typography.bodySmall.copy(
                    fontSize = 14.sp, lineHeight = 16.sp, fontWeight = FontWeight.W300, color = LocalContentColor.current.copy(alpha = 0.8f)
                )) {
                    text?.invoke()
                }
            }
            action?.invoke()
        }
    }
}

internal object MenuTokens {
    val ContentPaddingHorizontal = 24.dp
    val ContentPaddingVertical = 12.dp
    val ElementHorizontalSpace = 24.dp
}