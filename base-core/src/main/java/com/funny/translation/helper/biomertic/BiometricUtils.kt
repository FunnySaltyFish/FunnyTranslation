package com.funny.translation.helper.biomertic

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import com.funny.translation.AppConfig
import com.funny.translation.BaseApplication
import com.funny.translation.core.R
import com.funny.translation.helper.Log
import com.funny.translation.helper.UserUtils
import com.funny.translation.helper.awaitDialog
import com.funny.translation.helper.handler.runOnUI
import com.funny.translation.helper.string
import com.funny.translation.helper.toastOnUi
import com.funny.translation.network.ServiceCreator
import com.funny.translation.network.api
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.M)
object BiometricUtils {
    private const val TAG = "BiometricUtils"
    private const val SHARED_PREFS_FILENAME = "biometric_prefs_v2"
    const val CIPHERTEXT_WRAPPER = "ciphertext_wrapper"
    private const val SECRET_KEY = "trans_key"

    // 用于注册时临时保存当前的指纹数据
    private var tempSetFingerPrintInfo = FingerPrintInfo()
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
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> string(R.string.err_no_fingerprint_device)
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> string(R.string.err_fingerprint_not_enabled)
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> string(R.string.err_no_fingerprint)
        else -> string(R.string.unknown_error)
    }

    fun init() {

    }

    suspend fun uploadFingerPrint(username: String) = withContext(Dispatchers.IO){
        fingerPrintService.saveFingerPrintInfo(
            username = username,
            did = AppConfig.androidId,
            encryptedInfo = tempSetFingerPrintInfo.encrypted_info,
            iv = tempSetFingerPrintInfo.iv,
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
        try {
            val cipher = cryptographyManager.getInitializedCipherForEncryption(secretKeyName)
            val biometricPrompt =
                BiometricPromptUtils.createBiometricPrompt(activity, authSuccess = { authResult ->
                    authResult.cryptoObject?.cipher?.apply {
                        val encryptedServerTokenWrapper =
                            cryptographyManager.encryptData("$username@$did", this)

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

                        onSuccess(ei, iv)
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
        }catch (e: Exception){
            e.printStackTrace()
            onFail()
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
                val email = api(UserUtils.userService::getUserEmail, username)
                if (email == null || email == ""){
                    activity.toastOnUi(R.string.not_registered)
                    return@launch
                }

                if (awaitDialog(
                        activity,
                        string(R.string.hint),
                        string(
                            R.string.tip_reset_fingerprint,
                            username,
                            UserUtils.anonymousEmail(email)
                        ),
                        string(R.string.confirm),
                        string(R.string.cancel),
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
                            onError(-2, string(R.string.validate_fingerprint_failed))
                        },
                        onError = onError,
                        onUsePassword = onUsePassword
                    )
                }
            }
        } else {
            val secretKeyName = SECRET_KEY
            try {
                val cipher = cryptographyManager.getInitializedCipherForDecryption(
                    secretKeyName, ciphertextWrapper.initializationVector
                )
                val biometricPrompt = BiometricPromptUtils.createBiometricPrompt(
                    activity,
                    authSuccess = { authResult ->
                        authResult.cryptoObject?.cipher?.let {
                            val plaintext =
                                cryptographyManager.decryptData(ciphertextWrapper.ciphertext, it)
                            Log.d(TAG, "validateFingerPrint: plainText: $plaintext")
                            //                                    SampleAppUser.fakeToken = plaintext
                            // Now that you have the token, you can query server for everything else
                            // the only reason we call this fakeToken is because we didn't really get it from
                            // the server. In your case, you will have gotten it from the server the first time
                            // and therefore, it's a real token.
                            onSuccess(
                                ciphertextWrapper.ciphertext.joinToString(","),
                                ciphertextWrapper.initializationVector.joinToString(",")
                            )
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
            }catch (e: Exception){
                e.printStackTrace()
                onFail()
            }
        }
    }

    fun clearFingerPrintInfo(username: String){
        tempSetFingerPrintInfo = FingerPrintInfo()
        tempSetUserName = ""
        cryptographyManager.clearCiphertextWrapperFromSharedPrefs(
            BaseApplication.ctx,
            SHARED_PREFS_FILENAME,
            Context.MODE_PRIVATE,
            username
        )
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
}