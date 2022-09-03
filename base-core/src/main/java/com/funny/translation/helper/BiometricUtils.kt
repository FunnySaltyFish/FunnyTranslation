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
import com.funny.translation.AppConfig
import com.funny.translation.BaseApplication
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
    var encrypted_info: String = "",
    var iv: String = "",
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

@RequiresApi(Build.VERSION_CODES.M)
object BiometricUtils {
    private const val TAG = "BiometricUtils"
    private const val KEY_NAME = "KEY_LOGIN"

    private const val KEY_ENCRYPTED_INFO = "KEY_ENCRYPTED_INFO"
    private const val KEY_VECTOR = "KEY_VECTOR"

    // 用于注册时临时保存当前的指纹数据
    var tempSetFingerPrintInfo = FingerPrintInfo()
    var tempSetUserName = ""

    private fun getSecretKey(): SecretKey? {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        // keyAlias 为密钥别名，可自己定义，加密解密要一致
        if (!keyStore.containsAlias(KEY_NAME)) {
            // 不包含改别名，重新生成
            // 秘钥生成器
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val builder = KeyGenParameterSpec.Builder(
                KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(false)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            keyGenerator.init(builder.build())
            keyGenerator.generateKey()
        }
        return keyStore.getKey(KEY_NAME, "FunnyTrans".toCharArray()) as? SecretKey
    }

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
        data: String,
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

        val biometricPrompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        onUsePassword()
                    } else {
                        onError(errorCode, errString)
                    }
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

                    kotlin.runCatching {
                        val arr = data.split("@")
                        val encryptedInfo1 = encryptedInfo.joinToString(",")
                        val iv = cipher.iv.joinToString(",")

                        // 保存临时设置的
                        tempSetUserName = arr[0]
                        tempSetFingerPrintInfo.iv = iv
                        tempSetFingerPrintInfo.encrypted_info = encryptedInfo1

                        onSuccess(encryptedInfo1, iv)
                    }.onFailure {
                        it.printStackTrace()
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
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            runOnUI {
                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }
        } catch (e: Exception) {
            Log.e(TAG, "认证指纹时报错：", e)
        }
    }

    fun validateFingerPrint(
        activity: FragmentActivity,
        data: String,
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
                    decodeCipher.init(Cipher.DECRYPT_MODE, getSecretKey(), iv)

                    val biometricPrompt =
                        BiometricPrompt(activity, ContextCompat.getMainExecutor(activity),
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationError(
                                    errorCode: Int,
                                    errString: CharSequence
                                ) {
                                    super.onAuthenticationError(errorCode, errString)
                                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                                        onUsePassword()
                                    } else {
                                        onError(errorCode, errString)
                                    }
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
                                            onSuccess(
                                                fingerPrintInfo.encrypted_info,
                                                fingerPrintInfo.iv
                                            )
                                        } else {
                                            throw Exception("指纹验证失败！")
                                        }

                                    }.onFailure {
                                        it.printStackTrace()
                                        onError(-2, it.message ?: "未知错误(${it.javaClass})")
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
                    when {
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
                                    onError = onError,
                                    onUsePassword = onUsePassword
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

    private fun generateSecretKey(keyGenParameterSpec: KeyGenParameterSpec) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

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