package com.aone.kisan365

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class DashboardActivity : Activity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 23 &&
            checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 365)
        }

        webView = WebView(this)
        webView.setBackgroundColor(android.graphics.Color.rgb(242, 247, 243))
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                runOnUiThread {
                    val allowed = request.resources.filter {
                        it == PermissionRequest.RESOURCE_VIDEO_CAPTURE
                    }.toTypedArray()
                    if (allowed.isNotEmpty() &&
                        (Build.VERSION.SDK_INT < 23 ||
                            checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
                        request.grant(allowed)
                    } else {
                        request.deny()
                    }
                }
            }
        }
        setContentView(webView)
        webView.loadUrl("file:///android_asset/index.html")
    }

    @Deprecated("Deprecated in Android")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }
}
