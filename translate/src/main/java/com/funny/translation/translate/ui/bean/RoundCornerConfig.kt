package com.funny.translation.translate.ui.bean

sealed class RoundCornerConfig {
    object Top : RoundCornerConfig()
    object Bottom : RoundCornerConfig()
    object All : RoundCornerConfig()
}