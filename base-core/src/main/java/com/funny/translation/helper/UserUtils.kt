package com.funny.translation.helper

import android.util.Log
import com.funny.translation.Consts
import com.funny.translation.bean.UserBean
import com.funny.translation.network.CommonData
import com.funny.translation.network.ServiceCreator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        @Field("phone") phone: String
    ): CommonData<UserBean>

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

}

object UserUtils {
    private const val TAG = "UserUtils"

    private val userService by lazy(LazyThreadSafetyMode.PUBLICATION){
        ServiceCreator.create(UserService::class.java)
    }

    private val VALID_POSTFIX = arrayOf("163.com", "qq.com", "gmail.com", "126.com", "sina.com", "sohu.com", "hotmail.com", "yahoo.com", "foxmail.com", "funnysaltyfish.fun")

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
    // 长度8-16位，包含大小写字母和数字，可以包含

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
    ) = withContext(Dispatchers.IO){
        val loginData = userService.login(username, password, password_type, email, "")
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
        val uid = DataSaverUtils.readData(Consts.KEY_USER_UID, -1)
        if (uid < 0) return@withContext
        try {
            val data = userService.refreshToken(uid)
            if (data.code == 50) {
                DataSaverUtils.saveData(Consts.KEY_JWT_TOKEN, data.data ?: "")
                Log.i(TAG, "refreshJwtToken: 刷新Token成功")
            }
        }catch (e: Exception){
            e.printStackTrace()
            Log.e(TAG, "refreshJwtToken: 失败")
        }
    }
}

class SignInException(s: String) : Exception(s)
class SignUpException(s: String) : Exception(s)
