package com.funny.translation.translate

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_BACK
import android.view.View.GONE
import android.view.View.VISIBLE
import android.webkit.*
import android.widget.ProgressBar
import androidx.activity.ComponentActivity

class WebViewActivity : ComponentActivity() {
    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
    lateinit var webView : WebView
    lateinit var progressBar: ProgressBar
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_webview)
        
        webView = findViewById(R.id.webview)
        progressBar = findViewById(R.id.progressbar)
        progressBar.progress = 0

        with(webView){
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    if(newProgress<100){
                        if(progressBar.visibility==GONE)
                            progressBar.visibility = VISIBLE
                        progressBar.progress = newProgress
                    }else{
                        progressBar.visibility= GONE
                    }
                }

                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    Log.d("WebView", "Source:${consoleMessage?.sourceId()} line:${consoleMessage?.lineNumber()} Message:${consoleMessage?.message()}")
                    return true
                }
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    if (
                        url.startsWith("wechat://") || url.startsWith("weixin://") || url.startsWith("alipays://") || (url.startsWith("alipay://")) || url.startsWith("afd://")
                    ) {
                        val intent = Intent()
                        intent.action = Intent.ACTION_VIEW
                        intent.data = Uri.parse(url)
                        startActivity(intent)
                    }else if (url.startsWith("https://wx.tenpay.com")) {
                        //H5微信支付要用，不然说"商家参数格式有误"
                        val extraHeaders = HashMap<String, String>()
                        extraHeaders["Referer"] = "https://afdian.net/"
                        view.loadUrl(url, extraHeaders)
                        return true
                    }
                    else{
                        view.loadUrl(url)
                    }
                    return true
                }

                override fun onReceivedSslError(
                    view: WebView?,
                    handler: SslErrorHandler,
                    error: SslError?
                ) {
                    handler.proceed() //表示等待证书响应
                    // handler.cancel();      //表示挂起连接，为默认方式
                    // handler.handleMessage(null);    //可做其他处理
                }
            }
        }

        val webSettings = webView.settings
        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.setGeolocationEnabled(true)
        //启用数据库
        webSettings.databaseEnabled = true

        // 若加载的 html 里有JS 在执行动画等操作，会造成资源浪费（CPU、电量）
        // 在 onStop 和 onResume 里分别把 setJavaScriptEnabled() 给设置成 false 和 true 即可
        //设置自适应屏幕，两者合用
        webSettings.useWideViewPort = true //将图片调整到适合webview的大小

        webSettings.loadWithOverviewMode = true // 缩放至屏幕的大小
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        //缩放操作
        webSettings.setSupportZoom(true) //支持缩放，默认为true。是下面那个的前提。
        webSettings.builtInZoomControls = true //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.displayZoomControls = false //隐藏原生的缩放控件

        val url = intent.getStringExtra("load_url")
        url?.let{
            webView.loadUrl(it)
        }
    }

    override fun onStop() {
        webView.settings.javaScriptEnabled = false
        super.onStop()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onResume() {
        super.onResume()
        webView.settings.javaScriptEnabled = true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            true
        }else{
            super.onKeyDown(keyCode, event)
        }
    }
}