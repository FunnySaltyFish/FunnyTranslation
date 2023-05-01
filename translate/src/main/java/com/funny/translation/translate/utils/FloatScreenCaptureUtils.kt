package com.funny.translation.translate.utils

import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.funny.translation.helper.ScreenUtils
import com.funny.translation.helper.VibratorUtils
import com.funny.translation.helper.handler.runOnUI
import com.funny.translation.translate.FunnyApplication
import com.funny.translation.translate.R
import com.funny.translation.translate.appCtx
import com.lzf.easyfloat.EasyFloat
import com.lzf.easyfloat.enums.ShowPattern
import com.lzf.easyfloat.enums.SidePattern
import com.smarx.notchlib.utils.ScreenUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.roundToInt

// 悬浮窗与截屏相关的工具类
// 避免 EasyFloatUtils 代码太长的，单独抽出来
object FloatScreenCaptureUtils {
    private const val TAG = "FloatScreenCaptureUtils"
    private const val TAG_SCREEN_CAPTURE_WINDOW = "screen_capture_window"
    private var initScreenCaptureWindow = false
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    var startRecordScreenJob: Job? = null

    // 截屏的区域
    private var startRecordOffset = (0f to 0f)

    internal var whetherInScreenCaptureMode = false

    private var lastX = 0f
    private var lastY = 0f

    // 获取 ScreenCaptureView 的左上角相对屏幕的坐标
    private fun getScreenCaptureLeftTop(motionEvent: MotionEvent): Pair<Float, Float> {
        val viewLeft = motionEvent.rawX - motionEvent.x
        val viewTop = motionEvent.rawY - motionEvent.y
        val statusBarHeight = EasyFloat.getFloatView(TAG_SCREEN_CAPTURE_WINDOW)?.let {
            if (ScreenUtils.isStatusBarVisible(it)) ScreenUtil.getStatusBarHeight(appCtx) else 0
        } ?: 0
        return viewLeft to viewTop - statusBarHeight
    }

    internal fun registerDrag(
        plusView: View?,
        motionEvent: MotionEvent,
    ) {
        // 1. 桌面显示悬浮球，悬浮球拖动到任意位置停两秒以上，在悬浮球左侧显示“+”图标。此时拖动悬浮球开始截屏，拖动到目的地松手后完成截屏，显示“确认”按钮，点击后保存截屏到本地，获取到Uri后开始图片翻译并显示结果。帮我实现

        var viewLeftTop = getScreenCaptureLeftTop(motionEvent)
        // Log.d(TAG, "viewLeftTop = $viewLeftTop")
        if (!whetherInScreenCaptureMode) {
            if (plusView?.alpha == 0.5f) {
                plusView.alpha = 1f
            }
            // 如果超过两秒没有移动或者移动范围很小，则开始选择截屏区域
            if (abs(viewLeftTop.first - lastX) > 10 || abs(viewLeftTop.second - lastY) > 10) {
                startRecordScreenJob?.cancel()
                startRecordScreenJob = coroutineScope.launch(Dispatchers.Default) {
                    delay(2000)
                    startRecordScreenJob = null
                    VibratorUtils.vibrate(100)

                    withContext(Dispatchers.Main) {
                        // 开始截屏
                        whetherInScreenCaptureMode = true
                        // 这一段代码实际上是延迟两秒后执行的，所以要重新算一下 viewLeftTop
                        viewLeftTop = getScreenCaptureLeftTop(motionEvent)
                        startRecordOffset = viewLeftTop.copy()
                        showScreenCaptureWindow()
                        updateScreenCaptureWindowPlace(
                            viewLeftTop.first, viewLeftTop.second
                        )
                    }
                }
            }
        } else {
            // 正在画截屏的区域
            updateScreenCaptureWindowPlace(
                viewLeftTop.first, viewLeftTop.second
            )
        }
        lastX = viewLeftTop.first
        lastY = viewLeftTop.second
    }

    internal fun registerDragEnd(
        plusView: View?
    ) {
        if (plusView?.alpha == 1f) {
            plusView.alpha = 0.5f
        }
        startRecordScreenJob?.cancel()
        if (whetherInScreenCaptureMode) {
            // 结束截屏
            whetherInScreenCaptureMode = false
            EasyFloat.getFloatView(TAG_SCREEN_CAPTURE_WINDOW)
                ?.findViewById<LinearLayout>(R.id.float_screen_capture_button_line)
                ?.visibility = View.VISIBLE
            // 保存截屏 TODO
        }
    }


    internal fun showScreenCaptureWindow() {
        if (!initScreenCaptureWindow) {
            EasyFloat.with(FunnyApplication.ctx)
                .setTag(TAG_SCREEN_CAPTURE_WINDOW)
                .setLayout(R.layout.layout_float_screen_capture) { view ->
                    view.findViewById<ImageButton>(R.id.cancel_button).setOnClickListener {
                        EasyFloat.hide(TAG_SCREEN_CAPTURE_WINDOW)
                    }
                    view.findViewById<ImageButton>(R.id.confirm_button).setOnClickListener {
                        EasyFloat.hide(TAG_SCREEN_CAPTURE_WINDOW)
                    }
                }
                .setDragEnable(true)
                .setShowPattern(ShowPattern.ALL_TIME)
                .setSidePattern(SidePattern.DEFAULT)
                .setImmersionStatusBar(true)
                .setGravity(Gravity.TOP or Gravity.START)
                .show()

            initScreenCaptureWindow = true
        } else {
            runOnUI {
                EasyFloat.getFloatView(TAG_SCREEN_CAPTURE_WINDOW)
                    ?.findViewById<LinearLayout>(R.id.float_screen_capture_button_line)
                    ?.visibility = View.GONE

                EasyFloat.show(TAG_SCREEN_CAPTURE_WINDOW)
            }
        }
        Log.d(TAG, "showScreenCaptureWindow")
    }

    private fun updateScreenCaptureWindowPlace(left: Float, top: Float) {
        // 悬浮窗的定位是以左上角为基础
        val x =
            if (left > startRecordOffset.first) startRecordOffset.first else left
        val y =
            if (top > startRecordOffset.second) startRecordOffset.second else top
        val width = abs(left - startRecordOffset.first)
        val height = abs(top - startRecordOffset.second)
//        Log.d(TAG, "updateScreenCaptureWindow: start = $startRecordOffset, left, top = ${left.roundToInt()}, ${top.roundToInt()}, x, y = $x,$y w,h = $width, $height,")
        EasyFloat.updateFloat(TAG_SCREEN_CAPTURE_WINDOW, x.roundToInt(), y.roundToInt(), width.roundToInt(), height.roundToInt())
    }

    internal fun hideScreenCaptureWindow() {
        Log.d(TAG, "hideScreenCaptureWindow")
        EasyFloat.hide(TAG_SCREEN_CAPTURE_WINDOW)
    }

    internal fun dismiss(){
        EasyFloat.dismiss(TAG_SCREEN_CAPTURE_WINDOW)
    }
}

@Composable
fun ScreenCaptureWindow(

) {

}