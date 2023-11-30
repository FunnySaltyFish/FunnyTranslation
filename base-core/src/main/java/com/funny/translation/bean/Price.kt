package com.funny.translation.bean

import com.funny.translation.core.BuildConfig
import java.math.BigDecimal
import java.math.RoundingMode

typealias Price = BigDecimal

fun Price.show(scale: Int = 2): String {
    return this.setScale(scale, RoundingMode.HALF_UP).toString()
}

fun Price.showWithUnit(scale: Int = 2): String {
    val unit = if (BuildConfig.FLAVOR == "google") "$" else "¥"
    return "$unit${this.setScale(scale, RoundingMode.HALF_UP).toString()}"
}

operator fun Price.times(num: Double): Price {
    return this.multiply(num.toBigDecimal())
}