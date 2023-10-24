package com.funny.translation.ui

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import com.funny.translation.debug.rememberSaveableStateOf

@Stable
fun Modifier.floatingActionBarModifier(
    initialOffset: Offset = Offset(-12f, -100f)
) = composed {
    var offset by rememberSaveableStateOf(
        value = initialOffset,
        saver = OffsetSaver,
    )
    this
        .fillMaxSize()
        .wrapContentSize(Alignment.BottomEnd)
        .offset { offset.asIntOffset() }
        .pointerInput(Unit) {
            detectDragGesturesAfterLongPress { change, dragAmount ->
                offset += dragAmount
            }
        }
}

private val OffsetSaver = listSaver<Offset, Float>(
    save = { listOf(it.x, it.y) },
    restore = { Offset(it[0], it[1]) }
)