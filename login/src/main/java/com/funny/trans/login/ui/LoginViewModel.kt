package com.funny.trans.login.ui

import android.content.Context
import android.os.Build
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.trans.login.R
import com.funny.translation.AppConfig
import com.funny.translation.BaseApplication.Companion.ctx
import com.funny.translation.bean.UserInfoBean
import com.funny.translation.helper.SignUpException
import com.funny.translation.helper.UserUtils
import com.funny.translation.helper.biomertic.BiometricUtils
import com.funny.translation.helper.displayMsg
import com.funny.translation.helper.string
import com.funny.translation.helper.toastOnUi
import com.funny.translation.network.api
import kotlinx.coroutines.launch

const val PASSWORD_TYPE_FINGERPRINT = "1"
const val PASSWORD_TYPE_PASSWORD = "2"

class LoginViewModel : ViewModel() {
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var email by mutableStateOf("")

    // 为了保护隐私，当从远程获取到邮箱时，需要隐藏邮箱
    var secureEmail by mutableStateOf(false)
    val displayEmail by derivedStateOf {
        if (secureEmail) {
            UserUtils.anonymousEmail(email)
        } else email
    }
    var verifyCode by mutableStateOf("")
    var inviteCode by mutableStateOf("")

    val isValidUsername by derivedStateOf { UserUtils.isValidUsername(username) }
    val isValidEmail by derivedStateOf { UserUtils.isValidEmail(email) }
    val isValidInviteCode by derivedStateOf { UserUtils.isValidInviteCode(inviteCode) }

    var finishSetFingerPrint by mutableStateOf(false)
    var finishValidateFingerPrint by mutableStateOf(false)

    // 1 -> 指纹
    // 2 -> 密码
    var passwordType by mutableStateOf(if(AppConfig.lowerThanM) PASSWORD_TYPE_PASSWORD else PASSWORD_TYPE_FINGERPRINT)
    // 当在新设备登录时，需要验证邮箱
    var shouldVerifyEmailWhenLogin by mutableStateOf(false)

    var encryptedInfo = ""
    var iv = ""

    private val userService = UserUtils.userService

    private fun clear(){
        email = ""
        verifyCode = ""
        finishSetFingerPrint = false
        finishValidateFingerPrint = false
        encryptedInfo = ""
        iv = ""
    }

    fun login(
        onSuccess: (UserInfoBean) -> Unit,
        onError: (String) -> Unit
    ){
        viewModelScope.launch {
            try {
                val userBean = if (passwordType == PASSWORD_TYPE_FINGERPRINT){
                    UserUtils.login(username, "${AppConfig.androidId}#$encryptedInfo#$iv", passwordType, email, if(shouldVerifyEmailWhenLogin) verifyCode else "")
                } else {
                    UserUtils.login(username, password, passwordType, email, "")
                }
                if (userBean != null) {
                    onSuccess(userBean)
                }else{
                    onError(string(R.string.login_failed_empty_result))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.displayMsg(string(R.string.login)))
            }
        }
    }

    fun register(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ){
        val successAction = {
            onSuccess()
            clear()
        }
        viewModelScope.launch {
            try {
                if (passwordType == PASSWORD_TYPE_FINGERPRINT){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && BiometricUtils.tempSetUserName != username)
                        throw SignUpException(string(R.string.different_fingerprint))
                    UserUtils.register(username, "${AppConfig.androidId}#$encryptedInfo#$iv", passwordType, email, verifyCode, "", inviteCode, successAction)
                } else {
                    UserUtils.register(username, password, passwordType, email, verifyCode, "", inviteCode, successAction)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.displayMsg(string(R.string.register)))
            }
        }

    }

    fun sendVerifyEmail(context: Context){
        viewModelScope.launch {
            api(userService::sendVerifyEmail, username, email) {
                error {
                    context.toastOnUi(string(R.string.error_sending_email))
                }
            }
        }
    }

    fun sendResetPasswordEmail(context: Context){
        viewModelScope.launch {
            api(userService::sendResetPasswordEmail, username, email) {
                error {
                    context.toastOnUi(string(R.string.error_sending_email))
                }
            }
        }
    }

    fun sendFindUsernameEmail(context: Context){
        viewModelScope.launch {
            api(userService::sendFindUsernameEmail, email) {
                error {
                    context.toastOnUi(string(R.string.error_sending_email))
                }
            }
        }
    }

    fun sendCancelAccountEmail(context: Context){
        viewModelScope.launch {
            api(userService::sendCancelAccountEmail, username, email) {
                error {
                    context.toastOnUi(string(R.string.error_sending_email))
                }
            }
        }
    }

    fun resetPassword(context: Context, onSuccess: () -> Unit){
        viewModelScope.launch {
            api(userService::resetPassword, username, password, verifyCode) {
                error {
                    context.toastOnUi(string(R.string.reset_password_failed))
                }
                success {
                    onSuccess()
                }
            }
        }
    }

    fun resetFingerprint() {
        finishSetFingerPrint = false
        finishValidateFingerPrint = false
        encryptedInfo = ""
        iv = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BiometricUtils.clearFingerPrintInfo(username)
        }
        ctx.toastOnUi(string(R.string.reset_fingerprint_success))
    }

    fun findUsername(onSuccess: (List<String>) -> Unit){
        viewModelScope.launch {
            api(userService::findUsername, email, verifyCode) {
                success {
                    onSuccess(it.data ?: emptyList())
                }
            }
        }
    }

    fun cancelAccount(onSuccess: () -> Unit){
        viewModelScope.launch {
            api(userService::cancelAccount, verifyCode) {
                addSuccess {
                    onSuccess()
                }
            }
        }
    }

    fun updateUsername(s: String) { username = s }
    fun updatePassword(s: String) { password = s }
}
