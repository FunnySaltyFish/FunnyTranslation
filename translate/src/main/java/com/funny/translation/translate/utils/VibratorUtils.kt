package com.funny.translation.translate.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.funny.translation.translate.FunnyApplication

@SuppressLint("WrongConstant")
object VibratorUtils {
    val vibrator by lazy{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = FunnyApplication.ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            FunnyApplication.ctx.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun vibrate(time:Long = 100){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE))
        } else vibrator.vibrate(time)
    }

    fun cancel(){
        vibrator.cancel()
    }
}