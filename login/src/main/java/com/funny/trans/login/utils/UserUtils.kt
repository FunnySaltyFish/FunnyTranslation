package com.funny.trans.login.utils

import com.funny.trans.login.bean.UserBean
import com.funny.translation.network.CommonData
import com.funny.translation.network.ServiceCreator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * ```python
 * @bp_user.route("/login", methods=["POST"])
def login():
username = request.form.get("username", "")
password = request.form.get("password", "")
password_type = request.form.get("password_type", "1")
email = request.form.get("email", "")
phone = request.form.get("phone", "")
response = {
"code": 50,
"error_msg": "",
"message": ""
}
try:
user = sign_in(username, password, password_type, email, phone)
response["message"] = "登陆成功！"
print(user)
response["data"] = user
except SignInException as e:
response["code"] = -1
response["error_msg"] = str(e)
return make_response(jsonify(response), 200)

@bp_user.route("/register", methods=["POST"])
def register():
username = request.form.get("username", "")
password = request.form.get("password", "")
password_type = request.form.get("password_type", "1")
email = request.form.get("email", "")
phone = request.form.get("phone", "")
response = {
"code": 50,
"error_msg": "",
"message": ""
}
try:
sign_up(username, password, password_type, email, phone)
except SignUpException as e:
response["code"] = -1
response["error_msg"] = str(e)
if username in SPEACIAL_USERNAME_TIP:
response["error_msg"] += "("+ SPEACIAL_USERNAME_TIP[username] +")"
return make_response(jsonify(response), 200)
response["message"] = "注册成功！"

if appDB.col_sponsor.find_one({"name": username}) is not None:
response["message"] += f"(您的用户名在赞助列表里哦，如果你是赞助者本人，可以凭相关证明免费领永久会员！感谢您在应用初期的支持~)"

if username in SPEACIAL_USERNAME_TIP:
response["message"] += "("+ SPEACIAL_USERNAME_TIP[username] +")"
return make_response(jsonify(response), 200)

 * ```
 */

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

}

object UserUtils {
    private val userService by lazy(LazyThreadSafetyMode.PUBLICATION){
        ServiceCreator.create(UserService::class.java)
    }

    private val VALID_POSTFIX = arrayOf("163.com", "qq.com", "gmail.com", "126.com", "sina.com", "sohu.com", "hotmail.com", "yahoo.com", "foxmail.com", "funnysaltyfish.fun")

    fun isValidUsername(username: String): Boolean {
        return "^[\\w\\W\\u4e00-\\u9fff_]{3,16}\$".toRegex().matches(username)
    }

    fun isValidEmail(email: String): Boolean {
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
     * @param verifyCode String
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

}

class SignInException(s: String) : Exception(s)
class SignUpException(s: String) : Exception(s)
