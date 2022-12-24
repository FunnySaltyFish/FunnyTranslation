package com.funny.translation.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer

@Stable
fun Modifier.touchToScale(
    scaleRatio: Float = 0.9f,
    onClick: () -> Unit = {}
): Modifier = composed {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    var isPressed by remember {
        mutableStateOf(false)
    }

    val realScaleRation by animateFloatAsState(
        if (isPressed) scaleRatio else 1f
    )

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press   -> isPressed = true
                is PressInteraction.Release -> isPressed = false
                is PressInteraction.Cancel  -> isPressed = false
            }
        }
    }
    this.then(Modifier.clickable(interactionSource = interactionSource, indication = null, onClick = onClick).graphicsLayer {
        scaleX = realScaleRation
        scaleY = realScaleRation
        transformOrigin = TransformOrigin.Center
    })
}
