package com.funny.translation.helper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class FastClickHandler(
    private val totalTimes: Int = 5,
    private val durationInMills: Int = 2000,
    val action: SimpleAction
) : () -> Unit {
    private var firstClickTime = 0L
    var times = 0
    override fun invoke() {
        if (firstClickTime == 0L) firstClickTime = System.currentTimeMillis()
        times++
        if (times == totalTimes) {
            if (System.currentTimeMillis() - firstClickTime < durationInMills) action()
            else {
                firstClickTime = 0L
            }
            times = 0
        }
    }
}

@Composable
fun rememberFastClickHandler(
    totalTimes: Int = 5,
    durationInMills: Int = 2000,
    action: SimpleAction
): FastClickHandler {
    return remember(totalTimes, durationInMills, action) {
        FastClickHandler(totalTimes, durationInMills, action)
    }
}