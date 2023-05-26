package com.funny.translation.translate.ui.widget

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.util.lerp

private const val TAG = "SwipeCrossFadeLayout"
enum class SwipeShowType {
    Main,
    Foreground
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeCrossFadeLayout(
    modifier: Modifier = Modifier,
    state: SwipeableState<SwipeShowType> = rememberSwipeableState(SwipeShowType.Main),
    mainUpper: @Composable () -> Unit,
    mainLower: @Composable () -> Unit,
    foreground: @Composable () -> Unit,
) {
    var containerHeight by remember { mutableStateOf(100) }
    var mainUpperHeight by remember { mutableStateOf(0) }
    var lowerPartHeight by remember { mutableStateOf(100) }
    SubcomposeLayout(
        modifier = modifier.swipeable(
            state = state,
            anchors = mapOf(
                0f to SwipeShowType.Main,
                lowerPartHeight.toFloat() to SwipeShowType.Foreground
            ),
            orientation = Orientation.Vertical,
            thresholds = { _, _ -> FractionalThreshold(0.5f) }
        )
    ) { constraints ->
        containerHeight = constraints.maxHeight
        // 先测量背景的下半部分
        val mainLowerPlaceable = subcompose(MainLowerKey, mainLower).first().measure(constraints.copy(
            minWidth = 0,
            minHeight = 0
        ))

        lowerPartHeight = mainLowerPlaceable.height
        val mainUpperPlaceable = subcompose(MainUpperKey, mainUpper).first().measure(constraints.copy(
            minWidth = 0,
            minHeight = 0,
            maxHeight = constraints.maxHeight - lowerPartHeight
        ))
        // 再测量背景的上半部分
        mainUpperHeight = mainUpperPlaceable.height

        val progress = (state.offset.value / lowerPartHeight).coerceIn(0f, 1f)
        val foregroundHeight = mainUpperHeight + progress * lowerPartHeight

        // Log.d(TAG, "progress: $progress, containerHeight: $containerHeight, mainPartHeight: $mainUpperHeight, foregroundHeight: $foregroundHeight, lowerPartHeight: $lowerPartHeight")
        val foregroundPlaceable = subcompose(ForegroundKey, foreground).first().measure(
            constraints.copy(
                minWidth = constraints.minWidth,
                minHeight = foregroundHeight.toInt(),
                maxWidth = constraints.maxWidth,
                maxHeight = foregroundHeight.toInt()
            )
        )
        layout(constraints.maxWidth, constraints.maxHeight) {
            if (progress != 1f) {
                mainUpperPlaceable.placeRelativeWithLayer(0, 0) {
                    alpha = 1f - progress
                }
                mainLowerPlaceable.placeRelativeWithLayer(0, containerHeight - lowerPartHeight) {
                    alpha = 1f - progress
                }
            }
            if (progress > 0.01f) {
                foregroundPlaceable.placeRelativeWithLayer(0, 0) {
                    alpha = lerp(0.5f, 1f, progress)
                    // shadowElevation = if (progress == 1f) 0f else 8f
                }
            }
        }
    }
}

private const val MainUpperKey = "SwipeCrossFadeLayoutUpper"
private const val MainLowerKey = "SwipeCrossFadeLayoutLower"
private const val ForegroundKey = "SwipeCrossFadeLayoutForeground"