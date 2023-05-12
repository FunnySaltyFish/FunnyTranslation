package com.funny.translation.translate.utils

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
import com.funny.translation.helper.BitmapUtil
import com.funny.translation.helper.ScreenUtils
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.appCtx
import com.funny.translation.translate.bean.FileSize
import java.nio.ByteBuffer


object ScreenCaptureUtilsBackup {
    internal val TEMP_CAPTURED_IMAGE_PATH = appCtx.externalCacheDir?.absolutePath + "/temp_captured_image.jpg"

    private val mediaProjectionManager: MediaProjectionManager by lazy {
        appCtx.getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null

    private val imageReader by lazy {
        ImageReader.newInstance(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight(), ImageFormat.JPEG, 1)
    }

    fun startCaptureScreen(resCode: Int, data: Intent, rect: Rect) {
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
                val bytes = BitmapUtil.compressImage(bitmap, FileSize.fromMegabytes(1).size)
                BitmapUtil.saveBitmap(bytes, TEMP_CAPTURED_IMAGE_PATH)
                appCtx.toastOnUi("截图保存成功")
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