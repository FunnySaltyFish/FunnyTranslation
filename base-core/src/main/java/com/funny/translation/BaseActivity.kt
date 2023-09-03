package com.funny.translation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.funny.translation.helper.LocaleUtils
import com.funny.translation.helper.PixelCopyUtils
import com.funny.translation.helper.getKeyColors
import com.smarx.notchlib.NotchScreenManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

open class BaseActivity : AppCompatActivity() {
    private lateinit var callback: OnBackPressedCallback

    companion object {
        private const val TAG = "BaseActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 状态栏沉浸
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//        enableEdgeToEdge()
        NotchScreenManager.getInstance().setDisplayInNotch(this)
//        autoUpdateStatusBarColor()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            autoUpdateNavigationBarColor()
        }

        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        }

        onBackPressedDispatcher.addCallback(this, callback)

    }

    override fun attachBaseContext(newBase: Context?) {
        val context = newBase?.let {
            LocaleUtils.getWarpedContext(it)
        }
        super.attachBaseContext(context)
    }

    // 每段时间自动读取 statusBar 的颜色，并根据色彩设置 isDarkIcons
    private fun autoUpdateStatusBarColor() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    // 获取 statusBar 区域的 Bitmap
                    val statusBarView =
                        window.decorView.findViewById<View>(android.R.id.statusBarBackground)
                    statusBarView?.let { view ->
                        // 获取显示区域的内容，转成 Bitmap
                        PixelCopyUtils.createBitmapFromView(window, view) { bitmap, success ->
                            if (success) {
                                // 获取 Bitmap 的平均颜色
                                val controller =
                                    ViewCompat.getWindowInsetsController(window.decorView)
                                controller?.isAppearanceLightStatusBars = !isBitmapLight(bitmap!!)
                            }
                        }
                    }
                    delay(1000)
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun autoUpdateNavigationBarColor() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                delay(1000)
                ViewCompat.getRootWindowInsets(window.decorView)?.let { windowInsetsCompat ->
                    Log.d(TAG, "onCreate: rootWindowInsets: $windowInsetsCompat")
                    while (true) {
                        // 获取 statusBar 区域的 Bitmap
                        PixelCopyUtils.createBitmapFromWindowInsets(
                            window, windowInsetsCompat, WindowInsetsCompat.Type.navigationBars(),
                        ) {
                            Log.d(TAG, "onCreate: navigationBarBitmap: $it")
                            if (it != null) {
                                val isLight = isBitmapLight(it)
                                Log.d(TAG, "autoUpdateNavigationBarColor: isLight: $isLight")
//                                val controller =
//                                    ViewCompat.getWindowInsetsController(window.decorView)
//                                controller?.isAppearanceLightNavigationBars = isLight
                                if (!isLight) {
                                    window.decorView.systemUiVisibility =
                                        window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                                } else {
                                    window.decorView.systemUiVisibility =
                                        window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                                }
//                                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                                window.navigationBarColor = Color.TRANSPARENT
                                it.recycle()
                            }
                        }
                        delay(1000)
                    }
                }
            }
        }
    }

    private fun isBitmapLight(bitmap: Bitmap): Boolean {
        // 快速判断一张图片是否是亮色
        val keyColor = bitmap.getKeyColors(1)[0]
        Log.d(TAG, "isBitmapLight: keyColor: $keyColor")
        return keyColor.toArgb() > Color.parseColor("#eeeeee")
    }
}