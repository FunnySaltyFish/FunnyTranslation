package com.funny.translation.helper

import com.funny.translation.core.R

fun Exception.displayMsg(action: String = "")
    = message ?: (action + string(R.string.failed_unknown_err))