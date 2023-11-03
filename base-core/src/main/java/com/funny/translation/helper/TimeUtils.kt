package com.funny.translation.helper

import java.util.Calendar
import java.util.Date

object TimeUtils {
    fun formatTime(
        time: Long,
        formatTemplate: String = "%4d-%02d-%02d %02d:%02d:%02d"
    ): String {
        val date = Date(time)
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        return formatTemplate.format(year, month, day, hour, minute, second)
    }

    /**
     * 2021-01-01 02:03:04
     * @return String
     */
    fun getNowStr(): String {
        return formatTime(System.currentTimeMillis())
    }

    fun getNowStrUnderline(): String {
        return formatTime(System.currentTimeMillis(), "%4d_%02d_%02d_%02d_%02d_%02d")
    }
}