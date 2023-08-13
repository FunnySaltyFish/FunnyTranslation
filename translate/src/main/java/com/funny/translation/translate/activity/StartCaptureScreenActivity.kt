package com.funny.translation.translate.activity

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Rect
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.funny.translation.BaseApplication
import com.funny.translation.translate.appCtx
import com.funny.translation.translate.service.CaptureScreenService

class StartCaptureScreenActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "StartCaptureScreenAc"
        private const val EXTRA_KEY_RECT = "rect"
        internal const val ACTION_INIT = "init"
        internal const val ACTION_CAPTURE = "capture"
        fun start(rect: Rect?) {
            // 但现在有个问题，跳转时先跳到了 TransActivity，再打开的当前 Activity
            // TODO 优化成直接打开当前 Activity
            val context = BaseApplication.getCurrentActivity()
            val intent =
                Intent(context, StartCaptureScreenActivity::class.java)
                    .putExtra(EXTRA_KEY_RECT, rect)
            if (context is Activity){
                Log.d(TAG, "start: context is Activity")
                context.startActivityForResult(intent, 1)
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                appCtx.startActivity(intent)
            }
        }
    }

    lateinit var mService: CaptureScreenService
    private lateinit var requestCaptureScreenLauncher: ActivityResultLauncher<Intent>
    private val mMediaProjectionManager: MediaProjectionManager by lazy {
        getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    var mBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rect: Rect? = intent.extras?.getParcelable(EXTRA_KEY_RECT)

        if (rect == null) {
            startService(Intent(this, CaptureScreenService::class.java))
        } else {
            startService(
                Intent(this, CaptureScreenService::class.java)
                    .setAction(ACTION_CAPTURE)
                    .putExtra("rect", rect)
            )
        }

        requestCaptureScreenLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.data != null) {
                if (rect == null) {
                    startService(
                        Intent(this, CaptureScreenService::class.java)
                            .setAction(ACTION_INIT)
                            .putExtra("code", result.resultCode)
                            .putExtra("data", result.data)
                    )
                }
            }
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, CaptureScreenService::class.java)
        bindService(intent, mConnection, BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        if (mBound) {
            unbindService(mConnection)
            mBound = false
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        overridePendingTransition(0, 0)
    }


    // 如果 mService 已经初始化，且 mService 已经有了 MediaProjection，那么直接 finish
    private fun requestCaptureScreenOrFinish() {
        if (CaptureScreenService.hasMediaProjection) {
            finish()
        } else {
            requestCaptureScreenLauncher.launch(
                mMediaProjectionManager.createScreenCaptureIntent(),
            )
        }
    }

    private val mConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(
            className: ComponentName,
            service: IBinder
        ) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as CaptureScreenService.LocalBinder
            mService = binder.service
            mBound = true

            requestCaptureScreenOrFinish()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }


}
