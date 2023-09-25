package com.funny.translation.network.service

import com.funny.translation.bean.UserInfoBean
import com.funny.translation.network.CommonData
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

@Serializable
data class InvitedUser(
    val uid: Int,
    val register_time: String
)

interface UserService {
    @POST("user/verify_email")
    @FormUrlEncoded
    suspend fun verifyEmail(
        @Field("email") email: String,
        @Field("verify_code") verifyCode: String
    ): CommonData<Unit>

    @POST("user/send_verify_email")
    @FormUrlEncoded
    suspend fun sendVerifyEmail(
        @Field("username") username: String,
        @Field("email") email: String
    ): CommonData<Unit>

    // sendFindUsernameEmail
    @POST("user/send_find_username_email")
    @FormUrlEncoded
    suspend fun sendFindUsernameEmail(
        @Field("email") email: String
    ): CommonData<Unit>

    // sendResetPasswordEmail
    @POST("user/send_reset_password_email")
    @FormUrlEncoded
    suspend fun sendResetPasswordEmail(
        @Field("username") username: String,
        @Field("email") email: String
    ): CommonData<Unit>

    // fun sendCancelAccountEmail(username: String, email: String)
    @POST("user/send_cancel_account_email")
    @FormUrlEncoded
    suspend fun sendCancelAccountEmail(
        @Field("username") username: String,
        @Field("email") email: String
    ): CommonData<Unit>


    @POST("user/register")
    @FormUrlEncoded
    suspend fun register(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("password_type") passwordType: String,
        @Field("email") email: String,
        @Field("phone") phone: String,
        @Field("invite_code") inviteCode: String
    ): CommonData<Unit>

    @POST("user/login")
    @FormUrlEncoded
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("password_type") passwordType: String,
        @Field("email") email: String,
        @Field("phone") phone: String,
        @Field("verify_code") verifyCode: String,
        @Field("did") did: String
    ): CommonData<UserInfoBean>

    @POST("user/logout")
    @FormUrlEncoded
    // uid: Int, did: String
    suspend fun logout(
        @Field("uid") uid: Int,
        @Field("did") did: String
    ): CommonData<Unit>

    @POST("user/get_user_info")
    @FormUrlEncoded
    suspend fun getInfo(
        @Field("uid") uid: Int
    ): CommonData<UserInfoBean>

    @POST("user/get_user_email")
    @FormUrlEncoded
    suspend fun getUserEmail(
        @Field("username") username: String
    ): CommonData<String>

    @POST("user/refresh_token")
    @FormUrlEncoded
    suspend fun refreshToken(
        @Field("uid") uid: Int
    ): CommonData<String>

    @POST("user/change_avatar")
    suspend fun uploadAvatar(
        @Body body: MultipartBody
    ): CommonData<String>

    // resetPassword
    @POST("user/reset_password")
    @FormUrlEncoded
    suspend fun resetPassword(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("code") code: String
    ): CommonData<Unit>

    // findUsername
    @POST("user/find_username_by_email")
    @FormUrlEncoded
    suspend fun findUsername(
        @Field("email") email: String,
        @Field("code") code: String
    ): CommonData<List<String>>

    // changeUsername
    @POST("user/change_username")
    @FormUrlEncoded
    suspend fun changeUsername(
        @Field("uid") uid: Int,
        @Field("new_username") username: String,
    ): CommonData<Unit>

    // cancelUser
    @POST("user/cancel_account")
    @FormUrlEncoded
    suspend fun cancelAccount(
        @Field("verify_code") verifyCode: String,
    ): CommonData<Unit>

    // generateInviteCode
    @POST("user/generate_invite_code")
    suspend fun generateInviteCode(): CommonData<String>

    // getInviteUsers
    @POST("user/get_invite_users")
    suspend fun getInviteUsers(): CommonData<List<InvitedUser>>
}