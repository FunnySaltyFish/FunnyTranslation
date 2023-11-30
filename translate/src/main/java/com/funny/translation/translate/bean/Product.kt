package com.funny.translation.translate.bean

import com.funny.translation.bean.Price
import com.funny.translation.bean.show
import com.funny.translation.bean.times

interface Product {
    val id: Any
    val origin_price: Price
    val discount: Double
    fun getRealPrice() = origin_price * discount

    // 保留两位小数，向下取
    fun getRealPriceStr() = getRealPrice().show()
}
