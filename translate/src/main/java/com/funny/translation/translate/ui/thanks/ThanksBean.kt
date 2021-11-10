package com.funny.translation.translate.ui.thanks

import java.util.Date

data class Sponsor(
    val name : String,
    val message : String? = null,
    val date : Date,
    val money : Int
)