package com.funny.translation.helper

import android.os.Build
import android.util.Log
import java.time.ZoneId
import java.util.*

data class FunnyDate(var year: Int = 0, var month: Int = 0, var day: Int = 0)
object DateUtils {
    const val TAG = "DateUtils"
    val isSpringFestival: Boolean by lazy{
        val time = today
        Log.d(TAG, "isSpringFestival: today is $time")
        when (time.year) {
            2022 -> when (time.month) {
                1 -> time.day == 31
                2 -> time.day in 1..7
                else -> false
            }
            2023 -> when (time.month) {
                1 -> time.day in 21..28
                else -> false
            }
            2024 -> when (time.month) {
                2 -> time.day in 9..16
                else -> false
            }
            else -> false
        }
    }

    private val today = FunnyDate().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val localDate = Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            day = localDate.dayOfMonth
            month = localDate.monthValue
            year = localDate.year
        } else {
            val date = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"))
            day = date.get(Calendar.DATE)
            month = date.get(Calendar.MONTH) + 1
            year = date.get(Calendar.YEAR)
        }
    }

}