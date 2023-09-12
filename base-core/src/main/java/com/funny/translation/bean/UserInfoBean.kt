package com.funny.translation.bean

import androidx.annotation.Keep
import com.funny.translation.helper.DateSerializerType1
import com.funny.translation.helper.TimeUtils
import kotlinx.serialization.Serializable
import java.util.Date
import kotlin.time.Duration.Companion.days

/**
 * class User:
        uid: int
        username: str
        password: str
        email: str
        phone: str
        # 密码类型
        # 1 - 指纹
        # 2 - 普通密码
        password_type: int = 2
        avatar_url: str = ""
        # vip相关信息
        vip_level = 0,
        vip_start_time = -1
        # 单位为天
        vip_duration = -1
 */
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
){
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

    fun canChangeUsername() =
        lastChangeUsernameTime?.time == null || System.currentTimeMillis() - lastChangeUsernameTime.time > 30.days.inWholeMilliseconds

    fun nextChangeUsernameTimeStr() = if (canChangeUsername()) {
        "--"
    } else {
        val nextTime = lastChangeUsernameTime!!.time + 30.days.inWholeMilliseconds
        TimeUtils.formatTime(nextTime)
    }
}


