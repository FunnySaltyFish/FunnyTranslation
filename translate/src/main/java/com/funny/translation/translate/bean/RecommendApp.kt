package com.funny.translation.translate.bean

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecommendApp(
    val name: String,
    val description: String,
    @SerialName("icon_url") val iconUrl: String,
    @SerialName("detail_url") val detailUrl: String
)
