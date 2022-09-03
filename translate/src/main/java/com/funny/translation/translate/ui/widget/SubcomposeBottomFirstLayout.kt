package com.funny.translation.translate.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints

@Composable
fun SubcomposeBottomFirstLayout(modifier: Modifier, bottom: @Composable () -> Unit, other: @Composable () -> Unit) {
    val TAG = "BottomFirstLayout"
    SubcomposeLayout(modifier) { constraints: Constraints ->
        var bottomHeight = 0
        val bottomPlaceables = subcompose("bottom", bottom).map {
            val placeable = it.measure(constraints.copy(minWidth = 0, minHeight = 0))
            bottomHeight = placeable.height
            placeable
        }
        val h = constraints.maxHeight - bottomHeight //        Log.d(TAG, "SubcomposeBottomFirstLayout: max:${constraints.maxHeight} bh:$bottomHeight")
        val otherPlaceables = subcompose("other", other).map {
            it.measure(constraints.copy(minHeight = 0, maxHeight = h))
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            bottomPlaceables[0].placeRelative(0, h)
            otherPlaceables[0].placeRelative(0, h - otherPlaceables[0].height)
        }
    }
}