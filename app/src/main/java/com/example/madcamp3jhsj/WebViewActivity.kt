package com.example.madcamp3jhsj

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class WebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        val webView = findViewById<WebView>(R.id.webView)

        // WebView 설정
        webView.webViewClient = WebViewClient() // URL이 WebView 내에서 열리도록 설정
        webView.settings.javaScriptEnabled = true // 필요 시 JavaScript 활성화

        // Intent로 전달받은 URL 로드
        val url = intent.getStringExtra("URL")
        if (url != null) {
            webView.loadUrl(url)
        } else {
            println("No URL found in Intent")
        }
    }
}
