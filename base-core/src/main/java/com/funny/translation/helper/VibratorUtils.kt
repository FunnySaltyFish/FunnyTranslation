package com.funny.translation.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.funny.translation.BaseApplication

@SuppressLint("WrongConstant")
object VibratorUtils {
    val vibrator by lazy{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = BaseApplication.ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            BaseApplication.ctx.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun vibrate(time:Long = 100){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE))
        } else vibrator.vibrate(time)
    }

    fun cancel(){
        vibrator.cancel()
    }
}