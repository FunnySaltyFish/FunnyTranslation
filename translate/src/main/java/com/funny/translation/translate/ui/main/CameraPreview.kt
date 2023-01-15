package com.funny.translation.translate.ui.main

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.funny.cmaterialcolors.MaterialColors
import com.funny.translation.translate.utils.getCameraProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "CameraPreview"

@SuppressLint("ClickableViewAccessibility")
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    imageCaptureUseCase: ImageCapture,
    cameraSelector: CameraSelector,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
) {
    val context = LocalContext.current
    var previewUseCase by remember { mutableStateOf<UseCase>(Preview.Builder().build()) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    var camera: Camera? by remember {
        mutableStateOf(null)
    }
    var focusOffset: Offset? by remember {
        mutableStateOf(null)
    }

    LaunchedEffect(key1 = focusOffset){
        if (focusOffset != null){
            delay(3000)
            focusOffset = null
        }
    }

    AndroidView(
        modifier = modifier.drawFocusRect(focusOffset),
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                this.scaleType = scaleType
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            previewView.setOnTouchListener { v, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> return@setOnTouchListener true
                    MotionEvent.ACTION_UP -> {
                        camera ?: return@setOnTouchListener true
                        // Get the MeteringPointFactory from PreviewView
                        val factory = previewView.meteringPointFactory

                        // Create a MeteringPoint from the tap coordinates
                        val point = factory.createPoint(motionEvent.x, motionEvent.y)
                        focusOffset = Offset(motionEvent.x, motionEvent.y)

                        // Create a MeteringAction from the MeteringPoint, you can configure it to specify the metering mode
                        val action = FocusMeteringAction.Builder(point).build()

                        // Trigger the focus and metering. The method returns a ListenableFuture since the operation
                        // is asynchronous. You can use it get notified when the focus is successful or if it fails.
                        camera!!.cameraControl.startFocusAndMetering(action)

                        return@setOnTouchListener true
                    }
                    else -> return@setOnTouchListener false
                }
            }

            previewUseCase = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            previewView
        }
    )

    DisposableEffect(previewUseCase) {
        coroutineScope.launch {
            val cameraProvider = context.getCameraProvider()
            try {
                // Must unbind the use-cases before rebinding them.
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, previewUseCase, imageCaptureUseCase
                )
            } catch (ex: Exception) {
                Log.e("CameraCapture", "Failed to bind camera use cases", ex)
            }
        }
        onDispose {

        }
    }
}

val DIRECTIONS = arrayListOf(
    1 to 1, 1 to -1, -1 to 1, -1 to -1
)

@Stable
private fun Modifier.drawFocusRect(
    focusOffset: Offset? = null,
    color: Color = MaterialColors.Orange700,
    rectTotalLength: Int = 100,
    rectVisiblePercent: Float = 0.5f,
    maxScale: Float = 1.3f,
    scaleDurationPercent: Float = 0.3f,
) = composed { 
    if (focusOffset == null){
        this
    } else {
        val anim = remember {
            Animatable(0f)
        }
        LaunchedEffect(key1 = focusOffset){
            anim.snapTo(0f)
            anim.animateTo(1f)
        }

        this.then(
            Modifier.drawWithContent {
                drawContent()
                DIRECTIONS.forEach { p ->
                    val fx = ((1-maxScale)/scaleDurationPercent*(anim.value-1)+maxScale)
                    val x = focusOffset.x + p.first * rectTotalLength / 2 * fx
                    val y = focusOffset.y + p.second * rectTotalLength / 2 * fx

                    val x2 = x-rectVisiblePercent/2*rectTotalLength*p.first
                    val y2 = y-rectVisiblePercent/2*rectTotalLength*p.second
                    drawLine(color, Offset(x, y), Offset(x, y2), strokeWidth = 8f)
                    drawLine(color, Offset(x, y), Offset(x2, y), strokeWidth = 8f)
                }
            }
        )
    }
}