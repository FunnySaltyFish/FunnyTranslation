package com.funny.translation.translate.ui.widget

import android.graphics.Path
import android.graphics.PathMeasure
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.toSize
import com.funny.cmaterialcolors.MaterialColors
import com.funny.translation.translate.utils.FunnyBiggerText
import kotlinx.coroutines.delay
import kotlin.random.Random

private const val TAG = "FlashWidget"

data class Particle(var x: Float, var y: Float, var targetX: Float, var targetY: Float, val radius: Float, val brush: Brush){
    var path = Path().apply {
        moveTo(x, y)
        // 在起点和终点的中点附近随机选择点作为控制点
        val controlX = (x + targetX) / 2 + (Math.random() * 1080 - 540).toFloat()
        val controlY = (y + targetY) / 2 + (Math.random() * 2160 - 1080).toFloat()
        quadTo(controlX, controlY, targetX, targetY)
    }

    private var pathMeasure = PathMeasure(path, false)
    private var length = pathMeasure.length
    private val pos = FloatArray(2)
    private val tan = FloatArray(2)

    /**
     * 从 path 的起点移动到指定的百分比
     * @param percent Float [0, 1.0]
     */
    fun moveTo(percent: Float){
        pathMeasure.getPosTan(length * percent, pos, tan)
        x = pos[0]
        y = pos[1]
    }

    fun resetExitTarget(canvasSize: Size){
        // 左右两侧
        if (Random.nextBoolean()) {
            targetX = if (Random.nextBoolean()) Random.nextFloat() * 100 else canvasSize.width - Random.nextFloat() * 100
            targetY = Random.nextFloat() * canvasSize.height
        }
        // 上下两侧
        else {
            targetX = Random.nextFloat() * canvasSize.width
            targetY = if (Random.nextBoolean()) Random.nextFloat() * 100 else canvasSize.height - Random.nextFloat() * 100
        }
        path = Path().apply {
            moveTo(x, y)
            lineTo(targetX, targetY)
        }
        pathMeasure = PathMeasure(path, false)
        length = pathMeasure.length
    }
}

fun DrawScope.drawParticle(particle: Particle){
    drawCircle(particle.brush, particle.radius, Offset(particle.x, particle.y))
}

class TextFlashCanvasState(var particleList: List<Particle>? = null){
    var animFinished by mutableStateOf(false)
}

@Composable
fun TextFlashCanvas(
    modifier: Modifier,
    state: TextFlashCanvasState,
    text: String,
    textStyle: TextStyle,
    enterAnimDuration: Int = 2000,
    showTextDuration: Long = 3000L,
    exitAnimDuration: Int = 300,
    content: @Composable () -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val textSize = remember { textMeasurer.measure(buildAnnotatedString { append(text) }, textStyle).size }
    var recompose by remember { mutableStateOf(false) }
    var startTime: Long
    var canvasSize: Size? = null
    LaunchedEffect(state.particleList){
        if(state.particleList != null){
            startTime = System.currentTimeMillis()
            var f = true
            loop@ while (f) {
                withFrameMillis {
                    val percent = (System.currentTimeMillis() - startTime) / enterAnimDuration.toFloat()
                    if (percent > 1f) {
                        f = false
                    }
                    state.particleList?.forEach { particle ->
                        particle.moveTo(percent)
                    }
                    recompose = !recompose
                }
            }
            delay(showTextDuration)

            while (canvasSize == null) delay(100)
            resetParticleTargets(state.particleList!!, canvasSize = canvasSize!!)
            // 退出动画
            startTime = System.currentTimeMillis()
            f = true
            loop@ while (f) {
                withFrameMillis {
                    val percent = (System.currentTimeMillis() - startTime) / exitAnimDuration.toFloat()
                    if (percent > 1f) {
                        f = false
                    }
                    state.particleList?.forEach { particle ->
                        particle.moveTo(percent)
                    }
                    recompose = !recompose
                }
            }
            state.animFinished = true
        }
    }

    Box(modifier = modifier.drawWithContent {

        drawContent()
        if (!state.animFinished) {
            if(state.particleList == null){
                val textRect = Rect(Offset((size.width - textSize.width) / 2f,  (size.height - textSize.height) / 2f), textSize.toSize())
                state.particleList = initParticleList(text, size, textRect, MaterialColors.YellowA400)
                canvasSize = size
            }

            @Suppress("UNUSED_EXPRESSION")
            // 触发重组，更新页面
            recompose

            drawRect(Color.Gray.copy(alpha = 0.8f))
            state.particleList?.forEach {
                drawParticle(it)
            }
        }
    }){
        content()
    }
}

private fun initParticleList(text: String, canvasSize: Size, textRect: Rect, fontColor: Color): MutableList<Particle> {
    val particleList = mutableListOf<Particle>()
    var data: ByteArray?
    var code: IntArray?
    val sw = textRect.width / text.length / 16
    val sh = textRect.height / 16
    for( i in text.indices) {
        code = FunnyBiggerText.getByteCode(text.substring(i, i+1))
        data = FunnyBiggerText.read(code[0], code[1])

        var byteCount = 0
        for (line in 0 until FunnyBiggerText.all_16_32) {
            for (k in 0 until FunnyBiggerText.all_2_4) {
                for (j in 0..7) {
                    if (data[byteCount].toInt() shr 7 - j and 0x1 == 1) {
                        // 出发点为屏幕边缘
                        var x: Float
                        var y: Float
                        // 左右两侧
                        if (Random.nextBoolean()) {
                            x = if (Random.nextBoolean()) Random.nextFloat() * 100 else canvasSize.width - Random.nextFloat() * 100
                            y = Random.nextFloat() * canvasSize.height
                        }
                        // 上下两侧
                        else {
                            x = Random.nextFloat() * canvasSize.width
                            y = if (Random.nextBoolean()) Random.nextFloat() * 100 else canvasSize.height - Random.nextFloat() * 100
                        }
                        val col = k * 8 + j
                        val radius = 8f
                        val particle = Particle(
                            x = x,
                            y = y,
                            targetX = textRect.left + sw * col + i * textRect.width / text.length + sw / 2,
                            targetY = textRect.top + sh * line + sh / 2,
                            radius = radius,
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White,
                                    fontColor
                                ),
                            )
                        )
                        particleList.add(particle)
                    }
                }
                byteCount++
            }
        }
    }
    return particleList
}

// 从当前位置更改目标位置，新目标为屏幕的边缘
private fun resetParticleTargets(particleList: List<Particle>, canvasSize: Size){
    particleList.forEach {
        it.resetExitTarget(canvasSize)
    }
}