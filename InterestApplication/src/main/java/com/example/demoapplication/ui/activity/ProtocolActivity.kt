package com.example.demoapplication.ui.activity

import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.demoapplication.R
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import kotlinx.android.synthetic.main.activity_protocol.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 用户隐私政策  https://devppsns.duluduludala.com/ppsns/protocol/privacyProtocol/v1.json   1
 * 用户协议 https://devppsns.duluduludala.com/ppsns/protocol/userProtocol/v1.json   2

 */
class ProtocolActivity : BaseActivity() {

    private var type = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_protocol)
        initView()
    }

    private fun initView() {
        type = intent.getIntExtra("type", 1)
        when (intent.getIntExtra("type", 1)) {
            1 -> {
                hotT1.text = "隐私条款"
            }
            2 -> {
                hotT1.text = "用户协议"
            }
        }

        btnBack.onClick {
            finish()
        }

        initWebview()
    }

    private fun initWebview() {
        if (type == 1) {
            webView.loadUrl("https://devppsns.duluduludala.com/ppsns/protocol/privacyProtocol/v1.json")
        }else if (type == 2) {
            webView.loadUrl("https://devppsns.duluduludala.com/ppsns/protocol/userProtocol/v1.json")
        }

        val webSettings = webView.settings
        webSettings.loadWithOverviewMode = true
        webSettings.setSupportZoom(false)

        webView.webViewClient =object :WebViewClient(){
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                view.loadUrl(request.url.toString())

                return true
            }
        }



    }
}
