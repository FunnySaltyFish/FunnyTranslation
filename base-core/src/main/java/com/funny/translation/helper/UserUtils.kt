package com.funny.translation.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.funny.translation.AppConfig
import com.funny.translation.bean.UserInfoBean
import com.funny.translation.network.CommonData
import com.funny.translation.network.ServiceCreator
import com.funny.translation.network.api
import com.funny.translation.network.apiNoCall
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
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

object UserUtils {
    private const val TAG = "UserUtils"
    // 头像最高尺寸
    private const val TARGET_AVATAR_SIZE = 256

    val userService by lazy(LazyThreadSafetyMode.PUBLICATION){
        ServiceCreator.create(UserService::class.java)
    }

    private val VALID_POSTFIX = arrayOf("163.com", "qq.com", "gmail.com", "126.com", "sina.com", "sohu.com", "hotmail.com", "yahoo.com", "foxmail.com", "funnysaltyfish.fun", "outlook.com")

    fun isValidUsername(username: String): Boolean {
        return "^[\\w\\u4e00-\\u9fff]{3,16}\$".toRegex().matches(username)
    }

    fun isValidEmail(email: String): Boolean {
        // 只允许主流邮箱后缀
        if (VALID_POSTFIX.all { !email.endsWith(it) }) {
            return false
        }
        return "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*\$".toRegex().matches(email)
    }

    // 密码校验
    // 长度8-16位，包含且必须至少包含大小写字母和数字，可以包含 [].#*
    fun isValidPassword(password: String): Boolean {
        if (password.length < 8 || password.length > 16) {
            return false
        }
        return "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d\\[\\]\\.#*]{8,16}\$".toRegex().matches(password)
    }

    // 邀请码，8 位，字母加数字
    fun isValidInviteCode(inviteCode: String): Boolean {
        return "^[a-zA-Z0-9]{8}\$".toRegex().matches(inviteCode)
    }


    /**
     * 注册
     * @param username String
     * @param password String 密码格式： did#encrypted_info#iv
     * @param password_type String
     * @param email String
     * @return UserBean?
     */
    suspend fun login(
        username: String,
        // 密码格式： did#encrypted_info#iv
        password: String,
        passwordType: String,
        email: String,
        verifyCode: String
    ) = api(userService::login, username, password, passwordType, email, "", verifyCode, AppConfig.androidId, rethrowErr = true) {
        fail {
            throw SignInException(it.displayErrorMsg)
        }
    }

    suspend fun register(
        username: String,
        password: String,
        passwordType: String,
        email: String,
        verifyCode: String,
        phone: String,
        inviteCode: String,
        onSuccess: SimpleAction,
    ): Unit? {
        val checkEmail = apiNoCall(userService::verifyEmail, email, verifyCode) {
            fail {
                throw SignUpException(it.displayErrorMsg)
            }
        }
        checkEmail.call(true)

        return api(userService::register, username, password, passwordType, email, phone, inviteCode, rethrowErr = true) {
            addSuccess {
                onSuccess()
            }
            fail {
                throw SignUpException(it.displayErrorMsg)
            }
        }
    }

    suspend fun uploadUserAvatar(context: Context, imgUri: Uri, filename: String, width: Int, height: Int, uid: Int) : String {
        var scale = 1
        while (width > TARGET_AVATAR_SIZE * scale || height > TARGET_AVATAR_SIZE * scale){
            scale *= 2
        }
        val bitmapOptions = BitmapFactory.Options().apply {
            inSampleSize = scale
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val ins = context.contentResolver.openInputStream(imgUri) ?: return ""
        ins.use {
//            val data = ins.readBytes()
            try {
                val bitmap = BitmapFactory.decodeStream(ins, null, bitmapOptions)
                val data = BitmapUtil.compressImage(bitmap, 1024 * 100) // 最大100kb
                val body = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("uid", uid.toString())
                    .addFormDataPart("avatar", filename, data.toRequestBody())
                    .build()
                val response = userService.uploadAvatar(body)
                if (response.code == 50){
                    return response.data ?: ""
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
        return ""
    }
}

class SignInException(s: String) : Exception(s)
class SignUpException(s: String) : Exception(s)
