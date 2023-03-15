package com.funny.translation.helper
import android.os.Build

object DeviceUtils {
    fun is64Bit(): Boolean {
        return Build.SUPPORTED_64_BIT_ABIS.isNotEmpty()
    }
}