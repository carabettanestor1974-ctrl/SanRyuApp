package com.shanryu.benitojuarez

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
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
    private var androidNavInsetCssPx = 0f

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(8, 9, 10)
        window.navigationBarColor = Color.rgb(8, 9, 10)

        webView = WebView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
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
            val density = resources.displayMetrics.density.coerceAtLeast(1f)
            val zoom = 0.72f
            androidNavInsetCssPx = (bars.bottom / density) / zoom

            // Dejamos que el HTML gestione el espacio inferior. Si ponemos
            // padding inferior al WebView, los elementos position:fixed
            // siguen tomando como referencia el viewport completo y pueden
            // quedar debajo de la barra de navegación de Android.
            view.setPadding(0, bars.top, 0, 0)
            applyAndroidLayoutFix()
            insets
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest) =
                assetLoader.shouldInterceptRequest(request.url)

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return false
            }

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

        val inset = androidNavInsetCssPx
        webView.evaluateJavascript("""
            (function() {
              var old = document.getElementById("shanryuAndroidFix");
              if (old) old.remove();

              var s = document.createElement("style");
              s.id = "shanryuAndroidFix";
              s.innerHTML = ":root{--android-nav-inset:${inset}px;}" +
                "html{zoom:0.72!important;overflow-x:hidden!important;}" +
                "body{width:138.888889%!important;overflow-x:hidden!important;}" +
                "#appView{padding-bottom:calc(86px + var(--android-nav-inset))!important;}" +
                ".bottom-nav{width:138.888889%!important;right:auto!important;bottom:var(--android-nav-inset)!important;}" +
                ".modal{width:138.888889%!important;right:auto!important;bottom:var(--android-nav-inset)!important;height:calc(100% - var(--android-nav-inset))!important;}" +
                ".sheet{max-height:calc(92vh - var(--android-nav-inset))!important;padding-bottom:20px!important;}" +
                "#coverView{overflow-y:auto!important;overflow-x:hidden!important;max-height:none!important;padding-bottom:calc(24px + var(--android-nav-inset))!important;}" +
                "#coverView .cover-stage{height:auto!important;min-height:100%!important;max-height:none!important;overflow:visible!important;}";
              document.head.appendChild(s);
            })();
        """.trimIndent(), null)
    }

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
