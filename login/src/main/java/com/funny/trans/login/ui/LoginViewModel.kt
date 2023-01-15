package com.funny.trans.login.ui

import android.content.Context
import android.os.Build
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.translation.bean.UserBean
import com.funny.translation.helper.SignUpException
import com.funny.translation.helper.UserUtils
import com.funny.translation.AppConfig
import com.funny.translation.helper.BiometricUtils
import com.funny.translation.helper.toastOnUi
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var email by mutableStateOf("")
    var verifyCode by mutableStateOf("")

    val isValidUsername by derivedStateOf { UserUtils.isValidUsername(username) }
    val isValidEmail by derivedStateOf { UserUtils.isValidEmail(email) }

    var finishSetFingerPrint by mutableStateOf(false)
    var finishValidateFingerPrint by mutableStateOf(false)

    // 1 -> 指纹
    // 2 -> 密码
    var passwordType by mutableStateOf(if(AppConfig.lowerThanM) "2" else "1")
    // 当在新设备登录时，需要验证邮箱
    var shouldVerifyEmailWhenLogin by mutableStateOf(false)

    var encryptedInfo = ""
    var iv = ""

    val loginData
        get() = username + "@" + AppConfig.androidId

    private fun clear(){
        email = ""
        verifyCode = ""
        finishSetFingerPrint = false
        finishValidateFingerPrint = false
        encryptedInfo = ""
        iv = ""
    }

    fun login(
        onSuccess: (UserBean) -> Unit,
        onError: (String) -> Unit
    ){
        viewModelScope.launch {
            try {
                val userBean = if (passwordType == "1"){
                    UserUtils.login(username, "${AppConfig.androidId}#$encryptedInfo#$iv", passwordType, email, if(shouldVerifyEmailWhenLogin) verifyCode else "")
                } else {
                    UserUtils.login(username, password, passwordType, email, "")
                }
                if (userBean != null) {
                    onSuccess(userBean)
                }else{
                    onError("登录失败，返回的用户信息为空！")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "登录失败，未知错误！")
            }
        }
    }

    fun register(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ){
        viewModelScope.launch {
            try {
                if (passwordType == "1"){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && BiometricUtils.tempSetUserName != username)
                        throw SignUpException("当前用户名与设置指纹时用户名不同，请重新设置指纹")
                    UserUtils.register(username, "${AppConfig.androidId}#$encryptedInfo#$iv", passwordType, email, verifyCode, "")
                } else {
                    UserUtils.register(username, password, passwordType, email, verifyCode, "")
                }
                onSuccess()
                clear()
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "注册失败，未知错误！")
            }
        }

    }

    fun sendVerifyEmail(context: Context){
        viewModelScope.launch {
            try {
                UserUtils.sendVerifyEmail(username, email)
                context.toastOnUi("验证邮件已发送，请注意查收~")
            } catch (e: Exception) {
                e.printStackTrace()
                context.toastOnUi("发送失败，请稍后再试~（${e.message}）")
            }
        }
    }

    fun sendResetPasswordEmail(context: Context){
        viewModelScope.launch {
            try {
                UserUtils.sendResetPasswordEmail(username, email)
                context.toastOnUi("重置密码邮件已发送，请注意查收~")
            } catch (e: Exception) {
                e.printStackTrace()
                context.toastOnUi("发送失败，请稍后再试~（${e.message}）")
            }
        }
    }

    fun sendFindUsernameEmail(context: Context){
        viewModelScope.launch {
            try {
                UserUtils.sendFindUsernameEmail(email)
                context.toastOnUi("找回用户名邮件已发送，请注意查收~")
            } catch (e: Exception) {
                e.printStackTrace()
                context.toastOnUi("发送失败，请稍后再试~（${e.message}）")
            }
        }
    }

    fun resetPassword(context: Context, onSuccess: () -> Unit){
        viewModelScope.launch {
            try {
                UserUtils.resetPassword(username, password, verifyCode)
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                context.toastOnUi(e.message ?: "重置密码失败，未知错误！")
            }
        }
    }

    fun findUsername(context: Context, onSuccess: (List<String>) -> Unit){
        viewModelScope.launch {
            try {
                val username = UserUtils.findUsername(email, verifyCode)
                onSuccess(username ?: emptyList())
            } catch (e: Exception) {
                e.printStackTrace()
                context.toastOnUi(e.message ?: "找回用户名失败，未知错误！")
            }
        }
    }


}