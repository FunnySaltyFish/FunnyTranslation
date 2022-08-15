package com.funny.translation.helper

import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.funny.translation.AppConfig
import com.funny.translation.BaseApplication
import java.nio.charset.Charset
import java.security.KeyStore
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object BiometricUtils {
    private const val TAG = "BiometricUtils"
    private const val KEY_NAME = "KEY_LOGIN"

    private const val KEY_ENCRYPTED_INFO = "KEY_ENCRYPTED_INFO"
    private const val KEY_VECTOR = "KEY_VECTOR"

    private val biometricManager by lazy {
        BiometricManager.from(BaseApplication.ctx)
    }

    private val promptInfo by lazy {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("验证指纹")
            .setSubtitle("请将手指放在指纹传感器上")
            .setNegativeButtonText("使用密码（不建议）")
            .build()
    }

    private fun checkBiometricAvailable(): String = when(biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
        BiometricManager.BIOMETRIC_SUCCESS -> ""
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "未检测到指纹识别设备"
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "指纹识别设备未启用"
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "设备未录入指纹"
        else -> "未知错误"
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun init(){
        generateSecretKey(KeyGenParameterSpec.Builder(
            KEY_NAME,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setUserAuthenticationRequired(true)
            // Invalidate the keys if the user has registered a new biometric
            // credential, such as a new fingerprint. Can call this method only
            // on Android 7.0 (API level 24) or higher. The variable
            // "invalidatedByBiometricEnrollment" is true by default.
            .apply { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                setInvalidatedByBiometricEnrollment(true)
            }}
            .build())
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun setFingerPrint(
        activity: FragmentActivity,
        onNotSupport: (msg: String) -> Unit = { _ -> },
        onSuccess: () -> Unit = {},
        onFail: () -> Unit = {},
        onError: (errorCode: Int, errString: CharSequence) -> Unit = { _, _ -> Unit }
    ){
        val error = checkBiometricAvailable()
        if (error != "") {
            onNotSupport(error)
            return
        }

        var secretKey = getSecretKey()
        if (secretKey == null){
            generateSecretKey(KeyGenParameterSpec.Builder(
                KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                // Invalidate the keys if the user has registered a new biometric
                // credential, such as a new fingerprint. Can call this method only
                // on Android 7.0 (API level 24) or higher. The variable
                // "invalidatedByBiometricEnrollment" is true by default.
                .apply { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setInvalidatedByBiometricEnrollment(true)
                }}
                .build())
            secretKey = getSecretKey()
        }

        val biometricPrompt = BiometricPrompt(activity, ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val cipher = result.cryptoObject?.cipher ?: error("cipter is null")
                    val encryptedInfo: ByteArray = cipher.doFinal(
                        AppConfig.androidId.toByteArray(Charset.defaultCharset())
                    )
                    Log.d(TAG, "Encrypted information: " +
                            encryptedInfo.contentToString()
                    )
                    DataSaverUtils.saveData(KEY_ENCRYPTED_INFO, encryptedInfo.joinToString(separator = ","))
                    DataSaverUtils.saveData(KEY_VECTOR, cipher.iv.joinToString(separator = ","))
                    onSuccess()
                }
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFail()
                }
            })

        try {
            val cipher = getCipher()
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }catch (e: Exception){
            Log.e(TAG, "认证指纹时报错：", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun generateSecretKey(keyGenParameterSpec: KeyGenParameterSpec) {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    private fun getSecretKey(): SecretKey? {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")

        // Before the keystore can be accessed, it must be loaded.
        keyStore.load(null)
        Log.d(TAG, "getSecretKey: AndroidId: ${AppConfig.androidId}")
        return keyStore.getKey(KEY_NAME, AppConfig.androidId?.toCharArray()) as? SecretKey
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getCipher(): Cipher {
        return Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7)
    }
}