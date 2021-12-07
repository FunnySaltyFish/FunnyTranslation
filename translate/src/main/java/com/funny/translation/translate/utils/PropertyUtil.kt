package com.funny.translation.translate.utils

import com.funny.translation.translate.FunnyApplication
import java.util.Properties

object PropertyUtil {
    private val securityProps by lazy {
        Properties().apply {
//            load(FunnyApplication.ctx.openFileInput("secure_data.properties"))
            load(FunnyApplication.ctx.assets.open("secure_data.properties"))
        }
    }

    fun getSecureData(name : String): String = try {securityProps.getProperty(name)}catch (e:Exception){""}
}