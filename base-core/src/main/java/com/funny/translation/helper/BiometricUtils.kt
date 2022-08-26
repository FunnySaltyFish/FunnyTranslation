package com.funny.translation.helper

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.funny.translation.BaseApplication
import com.funny.translation.awaitDialog
import com.funny.translation.helper.handler.runOnUI
import com.funny.translation.network.ServiceCreator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

private const val EXTRA_NO_USER = "no_user"
private const val EXTRA_NEW_DEVICE = "new_device"

@Keep
data class FingerPrintInfo(
    val encrypted_info: String = "",
    val iv: String = "",
    // 额外信息：
    // 为 "no_user" 代表此用户不存在
    // 为 “new_device#email" 代表此设备是新设备，后半部分为邮箱
    val extra: String = ""
)

interface FingerPrintService {
    @FormUrlEncoded
    @POST("user/get_finger_print_info")
    @Headers("Cache-Control: no-cache")
    suspend fun getFingerPrintInfo(
        @Field("username") username: String,
        @Field("did") did: String
    ): FingerPrintInfo

    @FormUrlEncoded
    @POST("user/save_finger_print_info")
    @Headers("Cache-Control: no-cache")
    suspend fun saveFingerPrintInfo(
        @Field("username") username: String,
        @Field("did") did: String,
        @Field("encrypted_info") encryptedInfo: String,
        @Field("iv") iv: String
    )
}

object BiometricUtils {
    private const val TAG = "BiometricUtils"
    private const val KEY_NAME = "KEY_LOGIN"

    private const val KEY_ENCRYPTED_INFO = "KEY_ENCRYPTED_INFO"
    private const val KEY_VECTOR = "KEY_VECTOR"

    private val biometricManager by lazy {
        BiometricManager.from(BaseApplication.ctx)
    }

    private val fingerPrintService by lazy(LazyThreadSafetyMode.PUBLICATION) {
        ServiceCreator.create(FingerPrintService::class.java)
    }

    private val promptInfo by lazy {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("验证指纹")
            .setSubtitle("请将手指放在指纹传感器上")
            .setNegativeButtonText("使用密码（不建议）")
            .build()
    }

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

    @RequiresApi(Build.VERSION_CODES.M)
    fun init() {

    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun setFingerPrint(
        activity: FragmentActivity,
        data: String,
        onNotSupport: (msg: String) -> Unit = { _ -> },
        onSuccess: (String, String) -> Unit = { _, _ -> },
        onFail: () -> Unit = {},
        onError: (errorCode: Int, errString: CharSequence) -> Unit = { _, _ -> }
    ) {
        val error = checkBiometricAvailable()
        if (error != "") {
            onNotSupport(error)
            return
        }

        var secretKey = getSecretKey(data)
        if (secretKey == null) {
            generateSecretKey(KeyGenParameterSpec.Builder(
                KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                // Invalidate the keys if the user has registered a new biometric
                // credential, such as a new fingerprint. Can call this method only
                // on Android 7.0 (API level 24) or higher. The variable
                // "invalidatedByBiometricEnrollment" is true by default.
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        setInvalidatedByBiometricEnrollment(true)
                    }
                }
                .build())
            secretKey = getSecretKey(data)
        }

        val biometricPrompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val cipher = result.cryptoObject?.cipher ?: error("cipher is null")
                    val encryptedInfo: ByteArray = cipher.doFinal(
                        data.toByteArray(Charset.defaultCharset())
                    )
                    Log.d(
                        TAG, "Encrypted information: " +
                                encryptedInfo.contentToString()
                    )
//                    DataSaverUtils.saveData(KEY_ENCRYPTED_INFO, encryptedInfo.joinToString(separator = ","))
//                    DataSaverUtils.saveData(KEY_VECTOR, cipher.iv.joinToString(separator = ",").also {
//                        Log.d(
//                            TAG,
//                            "onAuthenticationSucceeded: saved iv: $it"
//                        ) })

                    kotlin.runCatching {
                        scope.launch {
                            val arr = data.split("@")
                            val encryptedInfo1 = encryptedInfo.joinToString(",")
                            val iv = cipher.iv.joinToString(",")
                            fingerPrintService.saveFingerPrintInfo(
                                username = arr[0],
                                did = arr[1],
                                encryptedInfo = encryptedInfo1,
                                iv = iv
                            )
                            onSuccess(encryptedInfo1, iv)
                        }
                    }.onFailure {
                        onError(-2, it.message ?: "未知错误")
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFail()
                }
            })

        try {
            val cipher = getCipher()
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            runOnUI {
                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }
        } catch (e: Exception) {
            Log.e(TAG, "认证指纹时报错：", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun validateFingerPrint(
        activity: FragmentActivity,
        data: String,
        onNotSupport: (msg: String) -> Unit = { _ -> },
        onSuccess: (String, String) -> Unit = { _, _ -> },
        onFail: () -> Unit = {},
        onError: (errorCode: Int, errString: CharSequence) -> Unit = { _, _ -> },
        // 新设备登录时回调
        onNewFingerPrint: (email: String) -> Unit = {}
    ) {
        val error = checkBiometricAvailable()
        if (error != "") {
            onNotSupport(error)
            return
        }

        val secretKey: SecretKey = getSecretKey(data) ?: kotlin.run {
            onError(-1, "未查询到密钥信息，请先录入指纹！")
            return
        }

        val decodeCipher = getCipher()
        kotlin.runCatching {
            scope.launch {
                val fingerPrintInfo = fingerPrintService.getFingerPrintInfo(
                    username = data.split("@")[0],
                    did = data.split("@")[1]
                )
                val ivBytes = convertStringToByteArray(fingerPrintInfo.iv)
                if (ivBytes != null) {
                    Log.d(TAG, "validateFingerPrint: loaded iv: ${ivBytes.joinToString(",")}")
                    val iv = IvParameterSpec(ivBytes)
                    decodeCipher.init(Cipher.DECRYPT_MODE, secretKey, iv)

                    val biometricPrompt =
                        BiometricPrompt(activity, ContextCompat.getMainExecutor(activity),
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationError(
                                    errorCode: Int,
                                    errString: CharSequence
                                ) {
                                    super.onAuthenticationError(errorCode, errString)
                                    onError(errorCode, errString)
                                }

                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    super.onAuthenticationSucceeded(result)
                                    val cipher =
                                        result.cryptoObject?.cipher ?: error("cipher is null")
                                    runCatching {
                                        val decryptedInfo: ByteArray = cipher.doFinal(
                                            convertStringToByteArray(fingerPrintInfo.encrypted_info)
                                        )
                                        val decryptedText = String(decryptedInfo)
                                        Log.d(
                                            TAG,
                                            "onAuthenticationSucceeded: decrypted text: $decryptedText"
                                        )
                                        if (data == decryptedText) {
                                            onSuccess(fingerPrintInfo.encrypted_info, fingerPrintInfo.iv)
                                        } else {
                                            throw Exception("指纹验证失败！")
                                        }

                                    }.onFailure {
                                        onError(-2, it.message ?: "未知错误")
                                    }
                                }

                                override fun onAuthenticationFailed() {
                                    super.onAuthenticationFailed()
                                    onFail()
                                }
                            })

                    withContext(Dispatchers.Main) {
                        biometricPrompt.authenticate(
                            promptInfo,
                            BiometricPrompt.CryptoObject(decodeCipher)
                        )
                    }
                } else {
                    when  {
                        fingerPrintInfo.extra == EXTRA_NO_USER -> onError(-3, "用户不存在！")

                        fingerPrintInfo.extra.startsWith(EXTRA_NEW_DEVICE) ->
                            if (awaitDialog(
                                    activity,
                                    "提示",
                                    "您当前为新设备，是否录入此指纹并发送验证码以验证您的邮箱？",
                                    "是",
                                    "否",
                                )
                            ) {
                                setFingerPrint(
                                    activity,
                                    data,
                                    onSuccess = { encryptedInfo, iv ->
                                        onSuccess(encryptedInfo, iv)
                                        onNewFingerPrint(fingerPrintInfo.extra.substringAfter("#"))
                                    },
                                    onFail = {
                                        onError(-2, "新指纹录入失败")
                                    },
                                    onError = { code, err ->
                                        onError(code, err)
                                    }
                                )
                            }
                        else -> onError(-5, "未知错误！")
                    }
                }
            }
        }.onFailure {
            it.printStackTrace()
            onError(-1, it.message ?: "未知错误")
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun generateSecretKey(keyGenParameterSpec: KeyGenParameterSpec) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    private fun getSecretKey(data: String): SecretKey? {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")

        // Before the keystore can be accessed, it must be loaded.
        keyStore.load(null)
        Log.d(TAG, "getSecretKey: Data: $data")
        return keyStore.getKey(KEY_NAME, data.toCharArray()) as? SecretKey
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getCipher(): Cipher {
        return Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7
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