package com.funny.translation.helper

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.PixelCopy
import android.view.View
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.core.view.WindowInsetsCompat

object PixelCopyUtils {
    private const val TAG = "PixelCopyUtils"
    /**
     * 将View转换成Bitmap
     */
    fun createBitmapFromView(window: Window, view: View, callBack: (Bitmap?, Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888, true)
            convertLayoutToBitmap(
                window, view, bitmap
            ) { copyResult -> //如果成功
                if (copyResult == PixelCopy.SUCCESS) {
                    callBack(bitmap,true)
                }else{
                    callBack(null,false)
                }
            }
        } else {
            var bitmap: Bitmap? = null
            //开启view缓存bitmap
            view.isDrawingCacheEnabled = true
            //设置view缓存Bitmap质量
            view.drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
            //获取缓存的bitmap
            val cache: Bitmap = view.getDrawingCache()
            if (!cache.isRecycled) {
                bitmap = Bitmap.createBitmap(cache)
            }
            //销毁view缓存bitmap
            view.destroyDrawingCache()
            //关闭view缓存bitmap
            view.setDrawingCacheEnabled(false)
            callBack(bitmap,bitmap!=null)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun convertLayoutToBitmap(
        window: Window, view: View, dest: Bitmap,
        listener: PixelCopy.OnPixelCopyFinishedListener
    ) {
        //获取layout的位置
        val location = IntArray(2)
        view.getLocationInWindow(location)
        //请求转换
        PixelCopy.request(
            window,
            Rect(location[0], location[1], location[0] + view.width, location[1] + view.height),
            dest, listener, Handler(Looper.getMainLooper())
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createBitmapFromWindowInsets(window: Window, windowInsets: WindowInsetsCompat, type: Int, callback: (Bitmap?) -> Unit) {
        val insets = windowInsets.getInsets(type)
        Log.d(TAG, "createBitmapFromWindowInsets: insets: $insets")
        val rect = if (type == WindowInsetsCompat.Type.statusBars()) {
            Rect(0, 0, window.decorView.width, insets.bottom)
        } else if (type == WindowInsetsCompat.Type.navigationBars()) {
            Rect(0, window.decorView.height - insets.bottom, window.decorView.width, window.decorView.height)
        } else {
            Rect(0, 0, 0, 0)
        }
        if (rect.width() == 0 || rect.height() == 0) {
            callback(null)
            return
        }
        Log.d(TAG, "createBitmapFromWindowInsets: rect: $rect")
        val bitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888)
        PixelCopy.request(
            window, rect, bitmap, { copyResult ->
                if (copyResult == PixelCopy.SUCCESS) {
                    callback(bitmap)
                } else {
                    callback(null)

                }
            }, Handler(Looper.getMainLooper())
        )
    }

}