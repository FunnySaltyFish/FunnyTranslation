package com.funny.trans.login.utils

import android.content.Context
import android.util.Log
import com.funny.translation.BaseApplication
import com.tencent.soter.core.model.ConstantsSoter
import com.tencent.soter.wrapper.SoterWrapperApi
import com.tencent.soter.wrapper.wrap_biometric.SoterBiometricCanceller
import com.tencent.soter.wrapper.wrap_biometric.SoterBiometricStateCallback
import com.tencent.soter.wrapper.wrap_net.ISoterNetCallback
import com.tencent.soter.wrapper.wrap_net.IWrapUploadKeyNet
import com.tencent.soter.wrapper.wrap_task.AuthenticationParam
import com.tencent.soter.wrapper.wrap_task.InitializeParam

object SoterUtils {


    private const val SOTER_SCENE = 0
    private val canceller by lazy {
        SoterBiometricCanceller()
    }
    private const val TAG = "SoterUtils"

    class FunnyUploadAuthKey : IWrapUploadKeyNet {
        private var _callback: ISoterNetCallback<IWrapUploadKeyNet.UploadResult>? = null
        override fun setRequest(requestDataModel: IWrapUploadKeyNet.UploadRequest) {
            Log.d(TAG, "setRequest: ")
        }

        override fun execute() {
            Log.d(TAG, "execute: ")
        }

        override fun setCallback(callback: ISoterNetCallback<IWrapUploadKeyNet.UploadResult>?) {
            _callback = callback
        }

    }

    fun init() {
        val param = InitializeParam.InitializeParamBuilder()
            .setScenes(SOTER_SCENE) // 场景值常量，后续使用该常量进行密钥生成或指纹认证
            .build()
        SoterWrapperApi.init(BaseApplication.ctx, {}, param)
        SoterWrapperApi.prepareAuthKey({
            Log.d(TAG, "init: prepareAuthKey: $it")
        },false, true, SOTER_SCENE, FunnyUploadAuthKey(), null)
    }

    fun auth(context: Context, onCancel: () -> Unit, onSuccess: () -> Unit, onFailure: (errorCode: Int, errorMsg: String) -> Unit) {
        val param = AuthenticationParam.AuthenticationParamBuilder()
            .setScene(SOTER_SCENE)
            .setContext(context)
            // fingerprint
            .setBiometricType(ConstantsSoter.FINGERPRINT_AUTH)
            .setSoterBiometricCanceller(canceller)
            // .setPrefilledChallenge("test challenge")
            .setSoterBiometricStateCallback(object : SoterBiometricStateCallback {
                override fun onStartAuthentication() {
                    Log.d(TAG, "onStartAuthentication: ")
                }

                override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
                    Log.d(TAG, "onAuthenticationHelp: $helpCode, $helpString")
                }

                override fun onAuthenticationSucceed() {
                    Log.d(TAG, "onAuthenticationSucceed: ")
                }

                override fun onAuthenticationFailed() {
                    Log.d(TAG, "onAuthenticationFailed: ")
                }

                override fun onAuthenticationCancelled() {
                    onCancel()
                }

                override fun onAuthenticationError(errorCode: Int, errorString: CharSequence?) {
                    // TODO("Not yet implemented")
                    Log.d(TAG, "onAuthenticationError: $errorCode, $errorString")
                }

            }).build()
        SoterWrapperApi.requestAuthorizeAndSign({ result ->
            if (result.isSuccess) {
                onSuccess()
            } else {
                onFailure(result.errCode, result.errMsg)
            }
        }, param)
    }

    fun destroy(){
        SoterWrapperApi.release()
    }
}