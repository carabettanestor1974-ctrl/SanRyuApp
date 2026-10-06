package com.shanryu.benitojuarez

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var topInset = 0
    private var bottomInset = 0

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(8, 9, 10)
        window.navigationBarColor = Color.rgb(8, 9, 10)

        webView = WebView(this).apply {
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.rgb(8, 9, 10))
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.mediaPlaybackRequiresUserGesture = false
            settings.javaScriptCanOpenWindowsAutomatically = false
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
        }

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        ViewCompat.setOnApplyWindowInsetsListener(webView) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            topInset = bars.top
            bottomInset = bars.bottom

            val lp = view.layoutParams as ViewGroup.MarginLayoutParams
            lp.topMargin = topInset
            lp.bottomMargin = bottomInset
            view.layoutParams = lp

            applyAndroidLayoutFix()
            insets
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest) =
                assetLoader.shouldInterceptRequest(request.url)

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                false

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                applyAndroidLayoutFix()
            }
        }

        webView.webChromeClient = WebChromeClient()
        setContentView(webView)
        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html")
    }

    private fun applyAndroidLayoutFix() {
        if (!::webView.isInitialized) return

        webView.evaluateJavascript("""
            (function() {
              var old = document.getElementById("shanryuAndroidFix");
              if (old) old.remove();

              var s = document.createElement("style");
              s.id = "shanryuAndroidFix";
              s.innerHTML =
                "html{zoom:1!important;overflow-x:hidden!important;}" +
                "body{width:100%!important;max-width:100%!important;overflow-x:hidden!important;}" +
                ".bottom-nav{left:0!important;right:0!important;bottom:0!important;width:100%!important;}" +
                ".modal{left:0!important;right:0!important;bottom:0!important;width:100%!important;height:100%!important;}" +
                ".sheet{max-height:92vh!important;padding-bottom:24px!important;box-sizing:border-box!important;}" +
                "#appView{padding-bottom:86px!important;}" +
                "#coverView{overflow-y:auto!important;overflow-x:hidden!important;max-height:none!important;padding-bottom:24px!important;}" +
                "#coverView .cover-stage{height:auto!important;min-height:100%!important;max-height:none!important;overflow:visible!important;}";
              document.head.appendChild(s);
            })();
        """.trimIndent(), null)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        webView.stopLoading()
        webView.webChromeClient = null
        webView.destroy()
        super.onDestroy()
    }
}
