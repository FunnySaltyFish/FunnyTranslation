package com.funny.translation.translate.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.PixelFormat
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import com.funny.translation.helper.BitmapUtil
import com.funny.translation.helper.ScreenUtils
import com.funny.translation.helper.handler.runOnUI
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.*
import com.funny.translation.translate.activity.StartCaptureScreenActivity.Companion.ACTION_CAPTURE
import com.funny.translation.translate.activity.StartCaptureScreenActivity.Companion.ACTION_INIT
import com.funny.translation.translate.bean.FileSize
import com.funny.translation.translate.utils.DeepLinkManager
import java.io.File
import java.nio.ByteBuffer
import java.util.*

class CaptureScreenService : Service() {
    companion object {
        private const val TAG = "MediaProjectionService"
        private var mResultCode = 0
        private var mResultData: Intent? = null

        val hasMediaProjection get() = mResultData != null
        val TEMP_CAPTURED_IMAGE_PATH = appCtx.externalCacheDir?.absolutePath + "/temp_captured_image.jpg"
        val WHOLE_SCREEN_RECT = Rect(-1, -1, -1, -1)
    }

    private var mRect: Rect? = null

    private var mMediaProjectionManager: MediaProjectionManager? = null
    private var mMediaProjection: MediaProjection? = null

    private val screenWidth by lazy { ScreenUtils.getScreenWidth() }
    private val screenHeight by lazy { ScreenUtils.getScreenHeight() }
    private val densityDpi get() = ScreenUtils.getScreenDensityDpi()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()

        Log.d(TAG, "onStartCommand: intent: ${intent?.action} extras: ${intent?.extras} ")
        when (intent?.action) {
            ACTION_INIT -> {
                mResultCode = intent.getIntExtra("code", -1)
                mResultData = intent.getParcelableExtra("data")

                if (mResultData != null) {
                    init()
                }
            }

            ACTION_CAPTURE -> {
                mRect = intent.getParcelableExtra("rect")
                capture()
            }
        }

        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun startForeground() {
        startForeground(1, NotificationCompat.Builder(this, FunnyApplication.SCREEN_CAPTURE_CHANNEL_ID).build())
    }

    private val mBinder: IBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        val service: CaptureScreenService get() = this@CaptureScreenService
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    private fun capture() {
        setUpVirtualDisplay()
    }

    private fun init() {
        mMediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mMediaProjection =
            mMediaProjectionManager!!.getMediaProjection(mResultCode, mResultData!!)
    }

    @SuppressLint("WrongConstant")
    private fun setUpVirtualDisplay() {
        var virtualDisplay: VirtualDisplay?
        runOnUI {
            try {
                val width = screenWidth
                val height = screenHeight
                val mImageReader =
                    ImageReader.newInstance(
                        width,
                        height,
                        PixelFormat.RGBA_8888,
                        1
                    )
                virtualDisplay = mMediaProjection!!.createVirtualDisplay(
                    "CaptureScreen",
                    width,
                    height,
                    densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mImageReader.surface,
                    null,
                    null
                )
                mImageReader.setOnImageAvailableListener({ reader ->
                    onImageAvailable(virtualDisplay, reader)
                }, null)
            } catch (throwable: Throwable) {
                showError(R.string.failed_to_take_screenshot, throwable)
            }
        }
    }

    private fun onImageAvailable(virtualDisplay: VirtualDisplay?, reader: ImageReader) {
        try {
            val image = reader.acquireLatestImage()
            image?.use {
                val width = image.width
                val height = image.height
                val planes: Array<Image.Plane> = image.planes
                if (planes.isEmpty()) {
                    appCtx.toastOnUi(getString(R.string.screenshot_failed))
                    return
                }
                val buffer: ByteBuffer = planes[0].buffer
                val pixelStride: Int = planes[0].pixelStride
                val rowStride: Int = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * width
                var bitmap = Bitmap.createBitmap(
                    width + rowPadding / pixelStride,
                    height,
                    Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)
                Log.d(TAG, "onImageAvailable: $mRect")
                if (mRect != null && mRect != WHOLE_SCREEN_RECT) {
                    // 做裁剪
                    val rect = Rect(
                        mRect!!.left,
                        mRect!!.top,
                        mRect!!.right,
                        mRect!!.bottom
                    )
                    bitmap = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height())
                }
                //保存图片到本地
                val bytes = BitmapUtil.compressImage(bitmap, FileSize.fromMegabytes(1).size)
                BitmapUtil.saveBitmap(bytes, TEMP_CAPTURED_IMAGE_PATH)
                appCtx.toastOnUi(getString(R.string.save_screenshot_success))
                // 如果是全屏翻译，先裁剪一下
                startTranslate(doClip = (mRect == WHOLE_SCREEN_RECT))
            }
        } catch (throwable: Throwable) {
            showError(R.string.failed_to_save_screenshot, throwable)
        } finally {
            execSafely {
                reader.close()
                virtualDisplay?.release()
            }
        }
    }

    private fun startTranslate(doClip: Boolean){
        val fileUri = Uri.fromFile(File(TEMP_CAPTURED_IMAGE_PATH))
        TransActivityIntent.TranslateImage(DeepLinkManager.buildImageTransUri(imageUri = fileUri, doClip = doClip)).asIntent().let {
            // 已经存在，就带到前台
            it.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(it)
        }
    }

    private fun showError(@StringRes messageId: Int, throwable: Throwable) {
        toastOnUi(getString(messageId) + (if (BuildConfig.DEBUG) "\n\n$throwable" else ""))
        throwable.printStackTrace()
    }

    private inline fun execSafely(block: () -> Unit) {
        try {
            block()
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
    }
}