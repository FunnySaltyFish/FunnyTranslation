package com.funny.translation.helper

import java.util.*

object TimeUtils {
    fun formatTime(time: Long): String {
        val date = Date(time)
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        return "%4d-%02d-%02d %02d:%02d:%02d".format(year, month, day, hour, minute, second)
    }
}