package com.funny.jetsetting.core.ui

import android.os.SystemClock
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.Role

// from emo
// 防抖点击
fun Modifier.throttleClick(
    interactionSource: MutableInteractionSource,
    indication: Indication?,
    timeout: Int = 250,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "throttleClick"
        properties["timeout"] = timeout
        properties["enabled"] = enabled
        properties["onClickLabel"] = onClickLabel
        properties["role"] = role
        properties["onClick"] = onClick
        properties["indication"] = indication
        properties["interactionSource"] = interactionSource
    }
) {
    val throttleHandler = remember(timeout) { ThrottleHandler(timeout) }
    Modifier.clickable(
        interactionSource = interactionSource,
        indication = indication,
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onClick = { throttleHandler.process(onClick) }
    )
}

internal class ThrottleHandler(private val timeout: Int = 200) {

    private var last: Long = 0

    fun process(event: () -> Unit) {
        val now = SystemClock.uptimeMillis()
        if (now - last > timeout) {
            event.invoke()
            last = now
        }
    }
}