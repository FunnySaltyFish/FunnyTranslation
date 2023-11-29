package com.funny.translation.translate.bean

import java.math.BigDecimal
import java.math.RoundingMode

interface Product {
    val id: Int
    val price: Double
    val discount: Double
    fun getRealPrice() = BigDecimal(price) * BigDecimal(discount)

    // 保留两位小数，向下取
    fun getRealPriceStr() = getRealPrice().setScale(2, RoundingMode.HALF_UP).toString()
}