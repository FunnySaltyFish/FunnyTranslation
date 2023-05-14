package com.funny.translation.translate.service

import android.annotation.SuppressLint
import android.app.*
import android.content.ContentValues
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.JPEG
import android.graphics.Bitmap.Config
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.MediaStore.Audio
import android.provider.MediaStore.Images.Media
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.funny.translation.helper.BitmapUtil
import com.funny.translation.helper.ScreenUtils
import com.funny.translation.helper.handler.runOnUI
import com.funny.translation.helper.toastOnUi
import com.funny.translation.translate.BuildConfig
import com.funny.translation.translate.R
import com.funny.translation.translate.activity.StartCaptureScreenActivity
import com.funny.translation.translate.activity.StartCaptureScreenActivity.Companion.ACTION_CAPTURE
import com.funny.translation.translate.activity.StartCaptureScreenActivity.Companion.ACTION_INIT
import com.funny.translation.translate.appCtx
import com.funny.translation.translate.bean.FileSize
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*

class CaptureScreenService : Service() {
    companion object {
        private const val TAG = "MediaProjectionService"
        private var mResultCode = 0
        private var mResultData: Intent? = null

        val hasMediaProjection get() = mResultData != null
        val TEMP_CAPTURED_IMAGE_PATH = appCtx.externalCacheDir?.absolutePath + "/temp_captured_image.jpg"
    }

    private var overlayView: View? = null
    private var mRect: Rect? = null

    private val windowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }

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

        overlayView?.let { windowManager.removeView(it) }
    }

    private fun startForeground() {
        val serviceIntent = Intent(this, CaptureScreenService::class.java)
        serviceIntent.action = "stop"
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val stopIntent =
            PendingIntent.getService(this, 0, serviceIntent, flag)

        val activityIntent = Intent(this, StartCaptureScreenActivity::class.java)
            .putExtra("fromService", true)
        val contentIntent =
            PendingIntent.getActivity(this, 0, activityIntent, flag)

        val appName = getString(R.string.app_name)
        val notification =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel =
                    NotificationChannel(appName, appName, NotificationManager.IMPORTANCE_NONE)
                channel.lightColor = Color.BLUE
                channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)
                Notification.Builder(this, appName)
            } else {
                @Suppress("deprecation")
                Notification.Builder(this)
            }
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_camera)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentTitle(appName)
                .setContentIntent(contentIntent)
                .addAction(
                    run {
                        val stop = getString(R.string.stop)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            Notification.Action.Builder(
                                Icon.createWithResource(
                                    this,
                                    R.drawable.ic_stop
                                ),
                                stop,
                                stopIntent
                            )
                        } else {
                            @Suppress("deprecation")
                            Notification.Action.Builder(
                                R.drawable.ic_stop,
                                stop,
                                stopIntent
                            )
                        }.build()
                    }
                )
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                11,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(11, notification)
        }
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

    private var mMediaProjectionManager: MediaProjectionManager? = null
    private var mMediaProjection: MediaProjection? = null

    private fun init() {
        mMediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mMediaProjection =
            mMediaProjectionManager!!.getMediaProjection(mResultCode, mResultData!!)
    }

    @SuppressLint("WrongConstant")
    private fun setUpVirtualDisplay() {
        overlayView?.isVisible = false
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
                overlayView?.isVisible = true
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
                    appCtx.toastOnUi("截图失败")
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
                if (mRect != null) {
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
                appCtx.toastOnUi("截图保存成功")
            }
        } catch (throwable: Throwable) {
            showError(R.string.failed_to_save_screenshot, throwable)
        } finally {
            overlayView?.isVisible = true
            execSafely {
                reader.close()
                virtualDisplay?.release()
            }
        }
    }

    private fun showPhoto(photoUri: Uri) {
        try {
            startActivity(
                Intent()
                    .setAction(Intent.ACTION_VIEW)
                    .setDataAndType(photoUri, "image/*")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (throwable: Throwable) {
            showError(R.string.failed_to_view_image, throwable)
        }
    }

    private fun createBitmap(reader: ImageReader): Bitmap {
        var image: Image? = null
        try {
            image = reader.acquireLatestImage()

            val width = image.width
            val height = image.height

            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * width

            val bitmap = Bitmap.createBitmap(
                width + rowPadding / pixelStride, height,
                Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)
            return bitmap
        } finally {
            image?.close()
        }
    }

    private fun createCompressedImageFile(bitmap: Bitmap): File {
        try {
            val df = SimpleDateFormat("yyyyMMdd-HHmmss.sss", Locale.US)
            val formattedDate = df.format(System.currentTimeMillis())
            val imgName = "Screenshot_$formattedDate.jpg"
            val imageFile = File(getTmpDir(), imgName)
            FileOutputStream(imageFile).use {
                bitmap.compress(JPEG, 100, it)
            }
            return imageFile
        } finally {
            bitmap.recycle()
        }
    }

    private fun writeImageFileToMediaStore(imageFile: File): Uri {
        try {
            val resolver = contentResolver
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val collection =
                    Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val details = ContentValues().apply {
                    put(Media.DISPLAY_NAME, imageFile.name)
                    put(Media.RELATIVE_PATH, "Pictures/Screenshots")
                    put(Audio.Media.IS_PENDING, 1)
                }
                resolver.insert(collection, details)!!.apply {
                    resolver.openFileDescriptor(this, "w", null).use { pfd ->
                        Files.copy(Paths.get(imageFile.path), FileOutputStream(pfd?.fileDescriptor))
                    }
                    details.clear()
                    details.put(Audio.Media.IS_PENDING, 0)
                    resolver.update(this, details, null, null)
                }
            } else {
                @Suppress("deprecation")
                Uri.parse(
                    Media.insertImage(
                        resolver,
                        imageFile.path,
                        imageFile.name,
                        imageFile.name
                    )
                )
            }
        } finally {
            imageFile.delete()
        }
    }

    private fun showError(@StringRes messageId: Int, throwable: Throwable) {
        Toast.makeText(
            this,
            getString(messageId) + (if (BuildConfig.DEBUG) "\n\n$throwable" else ""),
            Toast.LENGTH_LONG
        )
            .show()
        throwable.printStackTrace()
    }

    private inline fun execSafely(block: () -> Unit) {
        try {
            block()
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
    }

    private fun getTmpDir() = externalCacheDir ?: cacheDir


}