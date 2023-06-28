package com.funny.translation.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.funny.translation.AppConfig
import com.funny.translation.Consts
import com.funny.translation.bean.UserBean
import com.funny.translation.network.CommonData
import com.funny.translation.network.OkHttpUtils
import com.funny.translation.network.ServiceCreator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

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


    @POST("user/register")
    @FormUrlEncoded
    suspend fun register(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("password_type") passwordType: String,
        @Field("email") email: String,
        @Field("phone") phone: String
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
    ): CommonData<UserBean>

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
    ): CommonData<UserBean>

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

}

object UserUtils {
    private const val TAG = "UserUtils"
    // 头像最高尺寸
    private const val TARGET_AVATAR_SIZE = 256

    private val userService by lazy(LazyThreadSafetyMode.PUBLICATION){
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
        password_type: String,
        email: String,
        verifyCode: String
    ) = withContext(Dispatchers.IO){
        val loginData = userService.login(username, password, password_type, email, "", verifyCode, AppConfig.androidId)
        if (loginData.code != 50) {
            throw SignInException(loginData.error_msg ?: "未知错误")
        }
        return@withContext loginData.data
    }

    suspend fun register(
        username: String,
        password: String,
        password_type: String,
        email: String,
        verifyCode: String,
        phone: String
    ) = withContext(Dispatchers.IO){
        val verifyData = userService.verifyEmail(email, verifyCode)
        if (verifyData.code != 50) {
            throw SignUpException("邮箱验证码错误")
        }

        val registerData = userService.register(username, password, password_type, email, phone)
        if (registerData.code != 50) {
            throw SignUpException(registerData.error_msg ?: "未知错误")
        }
        return@withContext registerData.data
    }

    suspend fun sendVerifyEmail(username: String, email: String) = withContext(Dispatchers.IO){
        val sendData = userService.sendVerifyEmail(username, email)
        if (sendData.code != 50) {
            throw Exception("发送验证码失败！（${sendData.error_msg}）")
        }
    }

    suspend fun sendResetPasswordEmail(username: String, email: String) = withContext(Dispatchers.IO){
        val sendData = userService.sendResetPasswordEmail(username, email)
        if (sendData.code != 50) {
            throw Exception("发送验证码失败！（${sendData.error_msg}）")
        }
    }

    suspend fun sendFindUsernameEmail(email: String) = withContext(Dispatchers.IO){
        val sendData = userService.sendFindUsernameEmail(email)
        if (sendData.code != 50) {
            throw Exception("发送验证码失败！（${sendData.error_msg}）")
        }
    }

    suspend fun getUserInfo(uid: Int) = withContext(Dispatchers.IO){
        if (uid < 0) return@withContext null
        val userInfoData = userService.getInfo(uid)
        if (userInfoData.code != 50){
            throw Exception("获取用户信息失败")
        }
        userInfoData.data
//        UserBean(username = "FunnySaltyFish", uid = 1, avatar_url = "https://img2.woyaogexing.com/2022/08/27/667cc0590584fd54!400x400.jpg", email = "", password = "", phone = "")
    }

    suspend fun getUserEmail(username: String) = withContext(Dispatchers.IO){
        if (username == "") return@withContext ""
        val userInfoData = userService.getUserEmail(username)
        if (userInfoData.code != 50){
            throw Exception(userInfoData.error_msg ?: "获取用户邮箱失败")
        }
        userInfoData.data ?: ""
//        UserBean(username = "FunnySaltyFish", uid = 1, avatar_url = "https://img2.woyaogexing.com/2022/08/27/667cc0590584fd54!400x400.jpg", email = "", password = "", phone = "")
    }

    // 依据uid刷新JWT Token，并保存到本地
    // 错误会在内部捕获
    suspend fun refreshJwtToken() = withContext(Dispatchers.IO){
        val uid = AppConfig.uid
        if (uid < 0) return@withContext
        try {
            val data = userService.refreshToken(uid)
            if (data.code == 50) {
                AppConfig.updateJwtToken(data.data ?: "")
                Log.i(TAG, "refreshJwtToken: 刷新Token成功")
            }
        }catch (e: Exception){
            e.printStackTrace()
            Log.e(TAG, "refreshJwtToken: 失败")
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

    suspend fun resetPassword(username: String, password: String, verifyCode: String) = withContext(Dispatchers.IO){
        val resetData = userService.resetPassword(username, password, verifyCode)
        if (resetData.code != 50) {
            throw Exception(resetData.error_msg ?: "未知错误")
        }
        return@withContext resetData.data
    }

    suspend fun findUsername(email: String, verifyCode: String) = withContext(Dispatchers.IO){
        val findData = userService.findUsername(email, verifyCode)
        if (findData.code != 50) {
            throw Exception(findData.error_msg ?: "未知错误")
        }
        return@withContext findData.data
    }
}

class SignInException(s: String) : Exception(s)
class SignUpException(s: String) : Exception(s)
