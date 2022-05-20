package com.funny.bottomnavigation.utils

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix

object BitmapUtils {
    fun getBitmapFromResources(re: Resources?, id: Int): Bitmap {
        return BitmapFactory.decodeResource(re, id)
    }

    fun getScaledBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val scaleWidth = targetWidth.toFloat() / width
        val scaleHeight = targetHeight.toFloat() / height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
    }

    fun getBigBitmapFromResources(re: Resources?, id: Int, targetWidth: Int, targetHeight: Int): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(re, id, options)
        //现在原始宽高以存储在了options对象的outWidth和outHeight实例域中
        val rawWidth = options.outWidth
        val rawHeight = options.outHeight
        var inSampleSize = 1
        if (rawWidth > targetWidth || rawHeight > targetHeight) {
            val ratioHeight = rawHeight.toFloat() / targetHeight
            val ratioWidth = rawWidth.toFloat() / targetWidth
            inSampleSize = ratioWidth.coerceAtMost(ratioHeight).toInt()
        }
        options.inSampleSize = inSampleSize
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeResource(re, id, options)
    }
}