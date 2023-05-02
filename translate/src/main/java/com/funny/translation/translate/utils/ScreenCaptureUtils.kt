package com.funny.translation.translate.utils

import android.content.Context
import android.content.Context.MEDIA_PROJECTION_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import com.funny.translation.helper.ScreenUtils
import com.funny.translation.translate.appCtx
import java.nio.ByteBuffer


object ScreenCaptureUtils {
    private val mediaProjectionManager: MediaProjectionManager by lazy {
        appCtx.getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null

    private val imageReader by lazy {
        ImageReader.newInstance(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight(), ImageFormat.JPEG, 1)
    }

    fun startCaptureScreen(context: Context, resCode: Int, data: Intent, rect: Rect) {
        mediaProjection = mediaProjectionManager.getMediaProjection(resCode, data)
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            ScreenUtils.getScreenWidth(),
            ScreenUtils.getScreenHeight(),
            ScreenUtils.getScreenDensityDpi(),
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface,
            null,
            null
        )
        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            image?.use {
                image.cropRect = rect
                val width = image.width
                val height = image.height
                val planes: Array<Image.Plane> = image.planes
                val buffer: ByteBuffer = planes[0].buffer
                val pixelStride: Int = planes[0].pixelStride
                val rowStride: Int = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * width
                val bitmap = Bitmap.createBitmap(
                    width + rowPadding / pixelStride,
                    height,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)
                //保存图片到本地
            }

        }, null)
    }

    fun stopCaptureScreen() {
        virtualDisplay?.release()
        virtualDisplay = null
        mediaProjection?.stop()
        mediaProjection = null
    }
}