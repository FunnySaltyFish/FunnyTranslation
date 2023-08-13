package com.funny.translation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.*
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.funny.translation.helper.WebViewResourceHelper
import com.funny.translation.jsBean.core.R
import com.funny.translation.theme.TransTheme
import com.funny.translation.ui.*

private const val TAG = "WebViewActivity"
class WebViewActivity : BaseActivity() {

    companion object {
        private var backEvent: (() -> Unit)? = null
        fun start(context: Context, url: String, backEvent: (() -> Unit)? = null) {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra("load_url",url)
            context.startActivity(intent)
            Companion.backEvent = backEvent
        }
    }

    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra("load_url") ?: ""

        setContent {
            TransTheme {
                if (url.isNotEmpty()){
                    WebViewPage(url = url)
                } else {
                    Text("当前没有打开的页面~",
                        Modifier
                            .fillMaxSize()
                            .wrapContentSize(Center))
                }
            }
        }
    }

    override fun onBackPressed() {
        handleBackPressed()
        super.onBackPressed()
    }

    private fun handleBackPressed(){
        Log.d(TAG, "handleBackPressed: ")
        backEvent?.invoke()
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewPage(url: String) {
    Column(
        Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        val context = LocalContext.current
        val webViewState = rememberWebViewState(url)
        if (webViewState.webViewLoadingState is WebViewLoadingState.Loading) {
            LinearProgressIndicator(
                progress = (webViewState.webViewLoadingState as WebViewLoadingState.Loading).progress,
                Modifier.fillMaxWidth()
            )
        }

        Surface(Modifier.fillMaxWidth()) {
            webViewState.pageTitle?.let {
                Text(text = if (it.length <= 16) it else it.substring(0, 16) + "...", modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(CenterHorizontally)
                    .offset(0.dp, 16.dp)
                )
            }

            IconButton(onClick = {
                context.startActivity(Intent().apply {
                    action = Intent.ACTION_VIEW
                    addCategory(Intent.CATEGORY_BROWSABLE)
                    data = Uri.parse(url)
                })
            }, modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.End)
                .offset(x = (-8).dp)) {
                Icon(painter = painterResource(id = R.drawable.ic_jump_out), contentDescription = "open in browser")
            }
        }


        WebView(state = webViewState, Modifier.fillMaxSize()) {
            WebView(context).apply {
                with(settings) {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    setGeolocationEnabled(true)
                    //启用数据库
                    databaseEnabled = true

                    // 若加载的 html 里有JS 在执行动画等操作，会造成资源浪费（CPU、电量）
                    // 在 onStop 和 onResume 里分别把 setJavaScriptEnabled() 给设置成 false 和 true 即可
                    //设置自适应屏幕，两者合用
                    useWideViewPort = true //将图片调整到适合webview的大小
                    loadWithOverviewMode = true // 缩放至屏幕的大小
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    //缩放操作
                    setSupportZoom(true) //支持缩放，默认为true。是下面那个的前提。
                    builtInZoomControls = true //设置内置的缩放控件。若为false，则该WebView不可缩放
                    displayZoomControls = false //隐藏原生的缩放控件
                }

                WebViewResourceHelper.addChromeResourceIfNeeded(context)
            }
        }
    }
}