package com.funny.translation.helper

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.funny.translation.AppConfig
import com.funny.translation.BaseApplication
import com.funny.translation.helper.biomertic.*
import com.funny.translation.helper.handler.runOnUI
import com.funny.translation.network.ServiceCreator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val EXTRA_NO_USER = "no_user"
private const val EXTRA_NEW_DEVICE = "new_device"

@RequiresApi(Build.VERSION_CODES.M)
object BiometricUtils {
    private const val TAG = "BiometricUtils"
    const val SHARED_PREFS_FILENAME = "biometric_prefs"
    const val CIPHERTEXT_WRAPPER = "ciphertext_wrapper"
    private const val SECRET_KEY = "trans_key"

    // 用于注册时临时保存当前的指纹数据
    var tempSetFingerPrintInfo = FingerPrintInfo()
    var tempSetUserName = ""

    private val fingerPrintService by lazy(LazyThreadSafetyMode.PUBLICATION) {
        ServiceCreator.create(FingerPrintService::class.java)
    }

    private val biometricManager by lazy {
        BiometricManager.from(BaseApplication.ctx)
    }

    private val cryptographyManager = CryptographyManager()

    private val scope by lazy {
        CoroutineScope(Dispatchers.IO)
    }

    private fun checkBiometricAvailable(): String = when (biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG
    )) {
        BiometricManager.BIOMETRIC_SUCCESS -> ""
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "未检测到指纹识别设备"
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "指纹识别设备未启用"
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "设备未录入指纹"
        else -> "未知错误"
    }

    fun init() {

    }

    suspend fun uploadFingerPrint(username: String) = withContext(Dispatchers.IO){
        fingerPrintService.saveFingerPrintInfo(
            username = username,
            did = AppConfig.androidId,
            encryptedInfo = tempSetFingerPrintInfo.encrypted_info,
            iv = tempSetFingerPrintInfo.iv
        )
    }

    // 设置指纹信息，相关内容会暂存，等到注册时提交
    fun setFingerPrint(
        activity: FragmentActivity,
        username: String,
        did: String,
        onNotSupport: (msg: String) -> Unit = { _ -> },
        onSuccess: (String, String) -> Unit = { _, _ -> },
        onUsePassword: () -> Unit = {},
        onFail: () -> Unit = {},
        onError: (errorCode: Int, errString: CharSequence) -> Unit = { _, _ -> }
    ) {
        val error = checkBiometricAvailable()
        if (error != "") {
            onNotSupport(error)
            return
        }

        val secretKeyName = SECRET_KEY
        val cipher = cryptographyManager.getInitializedCipherForEncryption(secretKeyName)
        val biometricPrompt =
            BiometricPromptUtils.createBiometricPrompt(activity, authSuccess = { authResult ->
                authResult.cryptoObject?.cipher?.apply {
                    val encryptedServerTokenWrapper = cryptographyManager.encryptData("$username@$did", this)

                    val ei = encryptedServerTokenWrapper.ciphertext.joinToString(",")
                    val iv = encryptedServerTokenWrapper.initializationVector.joinToString(",")

                    cryptographyManager.persistCiphertextWrapperToSharedPrefs(
                        encryptedServerTokenWrapper,
                        BaseApplication.ctx,
                        SHARED_PREFS_FILENAME,
                        Context.MODE_PRIVATE,
                        username
                    )

                    tempSetFingerPrintInfo.iv = iv
                    tempSetFingerPrintInfo.encrypted_info = ei
                    tempSetUserName = username

                    onSuccess(ei,iv)
                }
            }, authError = { errorCode: Int, errString: String ->
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    onUsePassword()
                } else {
                    onError(errorCode, errString)
                }
            }, onFail)
        val promptInfo = BiometricPromptUtils.createPromptInfo()
        runOnUI {
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }

    fun validateFingerPrint(
        activity: FragmentActivity,
        username: String,
        did: String,
        onNotSupport: (msg: String) -> Unit = { _ -> },
        onSuccess: (String, String) -> Unit = { _, _ -> },
        onFail: () -> Unit = {},
        onUsePassword: () -> Unit,
        onError: (errorCode: Int, errString: CharSequence) -> Unit = { _, _ -> },
        // 新设备登录时回调
        onNewFingerPrint: (email: String) -> Unit = {}
    ) {
        val error = checkBiometricAvailable()
        if (error != "") {
            onNotSupport(error)
            return
        }

        val ciphertextWrapper = cryptographyManager.getCiphertextWrapperFromSharedPrefs(
            BaseApplication.ctx,
            SHARED_PREFS_FILENAME,
            Context.MODE_PRIVATE,
            username
        )

        if (ciphertextWrapper == null) {
            scope.launch(Dispatchers.IO) {
                val email = kotlin.runCatching { UserUtils.getUserEmail(username) }.onFailure {
                    it.printStackTrace()
                }.getOrDefault("")
                if (email == ""){
                    activity.toastOnUi("您似乎没有注册过，请先注册账号吧~")
                    return@launch
                }

                if (awaitDialog(
                        activity,
                        "提示",
                        "当前账号($username)在本机保存的指纹信息似乎已被清空，是否重新添加指纹并发送验证码以验证您的邮箱(${anonymousEmail(email)})？",
                        "是",
                        "否",
                    )
                ) {
                    setFingerPrint(
                        activity,
                        username,
                        did,
                        onSuccess = { encryptedInfo, iv ->
                            onSuccess(encryptedInfo, iv)
                            onNewFingerPrint(email)
                        },
                        onFail = {
                            onError(-2, "指纹验证失败")
                        },
                        onError = onError,
                        onUsePassword = onUsePassword
                    )
                }
            }
        } else {
            val secretKeyName = SECRET_KEY

            val cipher = cryptographyManager.getInitializedCipherForDecryption(
                secretKeyName, ciphertextWrapper.initializationVector
            )
            val biometricPrompt =
                BiometricPromptUtils.createBiometricPrompt(
                    activity,
                    authSuccess = { authResult ->
                        ciphertextWrapper.let { textWrapper ->
                            authResult.cryptoObject?.cipher?.let {
                                val plaintext =
                                    cryptographyManager.decryptData(textWrapper.ciphertext, it)
                                Log.d(TAG, "validateFingerPrint: plainText: $plaintext")
                                //                                    SampleAppUser.fakeToken = plaintext
                                // Now that you have the token, you can query server for everything else
                                // the only reason we call this fakeToken is because we didn't really get it from
                                // the server. In your case, you will have gotten it from the server the first time
                                // and therefore, it's a real token.
                                onSuccess(textWrapper.ciphertext.joinToString(","), textWrapper.initializationVector.joinToString(","))
                            }
                        }
                    },
                    authError = { errorCode, errString ->
                        if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            onUsePassword()
                        } else {
                            onError(errorCode, errString)
                        }
                    },
                    onFail
                )
            val promptInfo = BiometricPromptUtils.createPromptInfo()
            runOnUI {
                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }

        }
    }


    private fun convertStringToByteArray(data: String): ByteArray? {
        if (data != "") {
            val arr = data.split(",")
            val byteArray = ByteArray(arr.size)
            arr.forEachIndexed { i, c ->
                byteArray[i] = c.toByte()
            }
            return byteArray
        }
        return null
    }

    private fun anonymousEmail(email: String): String {
        val arr = email.split("@")
        if (arr.size == 2){
            val prefix = arr[0]
            val suffix = arr[1]
            return if (prefix.length > 3){
                prefix.take(3) + "***" + prefix.takeLast(3) + "@" + suffix
            } else {
                "$prefix***@$suffix"
            }
        }
        return ""
    }
}