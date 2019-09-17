package com.sdy.jitangapplication.ui.activity

import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.kotlin.base.common.BaseConstant
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
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
        //支持javascript
        // 设置可以支持缩放
        webView.settings.setSupportZoom(true)
        // 设置出现缩放工具
        webView.settings.builtInZoomControls = true
        //扩大比例的缩放
        webView.settings.useWideViewPort = true

        webView.settings.blockNetworkImage = false//解决图片不显示
        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW//混合模式，允许https中加载http图片
        //自适应屏幕
        webView.settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        webView.settings.loadWithOverviewMode = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)

                return true

            }
//            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
//                view.loadUrl(request.url.toString())
//
//                return true
//            }
        }

        if (type == 1) {
            webView.loadUrl("${BaseConstant.SERVER_ADDRESS}protocol/privacyProtocol/v1.json")
        } else if (type == 2) {
            webView.loadUrl("${BaseConstant.SERVER_ADDRESS}protocol/userProtocol/v1.json")
        }


    }
}
