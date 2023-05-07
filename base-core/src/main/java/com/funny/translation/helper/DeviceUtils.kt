package com.funny.translation.helper
import android.content.Context
import android.media.AudioManager
import android.os.Build
import com.funny.translation.BaseApplication

object DeviceUtils {
    fun is64Bit(): Boolean {
        return Build.SUPPORTED_64_BIT_ABIS.isNotEmpty()
    }

    // 获取系统当前音量
    fun getSystemVolume(): Int {
        val audioManager = BaseApplication.ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    // 判断是否静音
    fun isMute() = getSystemVolume() == 0
}