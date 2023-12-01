package com.funny.translation.bean

import androidx.annotation.Keep
import com.funny.translation.helper.BigDecimalSerializer
import com.funny.translation.helper.DateSerializerType1
import com.funny.translation.helper.TimeUtils
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.Date
import kotlin.time.Duration.Companion.days

@Keep
@Serializable
data class UserInfoBean(
    val uid: Int = -1,
    val username: String = "",
    val password: String = "",
    val email: String = "",
    val phone: String = "",
    val password_type: String = "1",
    val avatar_url: String = "",
    val vip_level: Int = 0,
    @Serializable(with = DateSerializerType1::class) val vip_start_time: Date? = null,
    val vip_duration: Long = -1,
    val jwt_token: String = "",
    val img_remain_points: Float = 0.0f,
    @Serializable(with = DateSerializerType1::class) val lastChangeUsernameTime: Date? = null,
    val invite_code: String = "",
    val inviter_uid: Int = -1,
    @Serializable(with = BigDecimalSerializer::class)
    val ai_text_point: BigDecimal = BigDecimal.ZERO,
    @Serializable(with = BigDecimalSerializer::class)
    val ai_voice_point: BigDecimal = BigDecimal.ZERO,
) {
    fun isValid() = uid >= 0 && jwt_token != ""
    fun isValidVip() =
        isValid() && (vip_level > 0) && vip_start_time?.time != null
                && vip_start_time.time + vip_duration * 86400 * 1000 > System.currentTimeMillis()

    fun vipEndTimeStr() = if (isValidVip()) {
        val endTime = vip_start_time!!.time + vip_duration * 86400 * 1000
        TimeUtils.formatTime(endTime)
    } else {
        "--"
    }

    fun isSoonExpire() =
        isValidVip() && vip_start_time!!.time + vip_duration * 86400 * 1000 - System.currentTimeMillis() < 5.days.inWholeMilliseconds

    fun canChangeUsername() =
        lastChangeUsernameTime?.time == null || System.currentTimeMillis() - lastChangeUsernameTime.time > 30.days.inWholeMilliseconds

    fun nextChangeUsernameTimeStr() = if (canChangeUsername()) {
        "--"
    } else {
        val nextTime = lastChangeUsernameTime!!.time + 30.days.inWholeMilliseconds
        TimeUtils.formatTime(nextTime)
    }
}


