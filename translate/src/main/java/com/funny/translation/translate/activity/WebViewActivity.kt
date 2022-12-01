package com.funny.translation.translate.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.funny.translation.theme.TransTheme
import com.funny.translation.ui.*
import com.smarx.notchlib.NotchScreenManager

class WebViewActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context, url: String){
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra("load_url",url)
            context.startActivity(intent)
        }
    }

    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        NotchScreenManager.getInstance().setDisplayInNotch(this)

        val url = intent.getStringExtra("load_url") ?: ""

        setContent {
            TransTheme {
                SystemBarSettings()
                if (url.isNotEmpty()){
                    WebViewPage(url = url)
                }
            }

        }

//        setContentView(R.layout.layout_webview)
//
//        webView = findViewById(R.id.webview)
//        progressBar = findViewById(R.id.progressbar)
//        progressBar.progress = 0
//
//        with(webView){
//            webChromeClient = object : WebChromeClient() {
//                override fun onProgressChanged(view: WebView?, newProgress: Int) {
//                    super.onProgressChanged(view, newProgress)
//                    if(newProgress<100){
//                        if(progressBar.visibility==GONE)
//                            progressBar.visibility = VISIBLE
//                        progressBar.progress = newProgress
//                    }else{
//                        progressBar.visibility= GONE
//                    }
//                }
//
//                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
//                    Log.d("WebView", "Source:${consoleMessage?.sourceId()} line:${consoleMessage?.lineNumber()} Message:${consoleMessage?.message()}")
//                    return true
//                }
//            }
//
//            webViewClient = object : WebViewClient() {
//                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
//                    if (
//                        url.startsWith("wechat://") || url.startsWith("weixin://") || url.startsWith("alipays://") || (url.startsWith("alipay://")) || url.startsWith("afd://")
//                    ) {
//                        val intent = Intent()
//                        intent.action = Intent.ACTION_VIEW
//                        intent.data = Uri.parse(url)
//                        startActivity(intent)
//                    }else if (url.startsWith("https://wx.tenpay.com")) {
//                        //H5微信支付要用，不然说"商家参数格式有误"
//                        val extraHeaders = HashMap<String, String>()
//                        extraHeaders["Referer"] = "https://afdian.net/"
//                        view.loadUrl(url, extraHeaders)
//                        return true
//                    }
//                    else{
//                        view.loadUrl(url)
//                    }
//                    return true
//                }
//
//                override fun onReceivedSslError(
//                    view: WebView?,
//                    handler: SslErrorHandler,
//                    error: SslError?
//                ) {
//                    handler.proceed() //表示等待证书响应
//                    // handler.cancel();      //表示挂起连接，为默认方式
//                    // handler.handleMessage(null);    //可做其他处理
//                }
//            }
//        }
//
//        val webSettings = webView.settings
//        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
//        webSettings.javaScriptEnabled = true
//        webSettings.domStorageEnabled = true
//        webSettings.setGeolocationEnabled(true)
//        //启用数据库
//        webSettings.databaseEnabled = true
//
//        // 若加载的 html 里有JS 在执行动画等操作，会造成资源浪费（CPU、电量）
//        // 在 onStop 和 onResume 里分别把 setJavaScriptEnabled() 给设置成 false 和 true 即可
//        //设置自适应屏幕，两者合用
//        webSettings.useWideViewPort = true //将图片调整到适合webview的大小
//
//        webSettings.loadWithOverviewMode = true // 缩放至屏幕的大小
//        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
//        //缩放操作
//        webSettings.setSupportZoom(true) //支持缩放，默认为true。是下面那个的前提。
//        webSettings.builtInZoomControls = true //设置内置的缩放控件。若为false，则该WebView不可缩放
//        webSettings.displayZoomControls = false //隐藏原生的缩放控件
//
//        url = intent.getStringExtra("load_url")
//        url?.let{
//            webView.loadUrl(it)
//        }
    }

    override fun onStop() {
//        webView.settings.javaScriptEnabled = false
        super.onStop()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onResume() {
        super.onResume()
//        if(DataSaverUtils.readData(Consts.KEY_HIDE_STATUS_BAR, true)){
//            // Hide the status bar.
//            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
//            // Remember that you should never show the action bar if the
//            // status bar is hidden, so hide that too if necessary.
//            supportActionBar?.hide()
//        }
//        webView.settings.javaScriptEnabled = true
    }

    private fun openWithBrowser(url:String){
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.data = Uri.parse(url)
        startActivity(intent)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewPage(url: String) {
    Column(Modifier.fillMaxSize().systemBarsPadding()) {
        val context = LocalContext.current
        val webView = remember {
            WebView(context).apply {
                with(settings){
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
                webViewClient = CustomWebViewClient()
                webChromeClient = AccompanistWebChromeClient()
            }
        }
        val webViewState = rememberWebViewState(url)
        if (webViewState.loadingState is LoadingState.Loading){
            LinearProgressIndicator(progress = (webViewState.loadingState as LoadingState.Loading).progress, Modifier.fillMaxWidth())
        }

        WebView(state = webViewState, Modifier.fillMaxSize()){
            webView
        }
    }
}

private class CustomWebViewClient: AccompanistWebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val url = request?.url?.toString() ?: return false
        view?.context ?: return true
        if (
            url.startsWith("wechat://") || url.startsWith("weixin://") || url.startsWith("alipays://") || (url.startsWith("alipay://")) || url.startsWith("afd://")
        ) {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.data = Uri.parse(url)
            view.context.startActivity(intent)
        }else if (url.startsWith("https://wx.tenpay.com")) {
            //H5微信支付要用，不然说"商家参数格式有误"
            val extraHeaders = HashMap<String, String>()
            extraHeaders["Referer"] = "https://afdian.net/"
            view.loadUrl(url, extraHeaders)
            return true
        }

        return super.shouldOverrideUrlLoading(view, request)
    }
}