package com.sdy.jitangapplication.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.kotlin.base.common.BaseConstant
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseActivity
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import kotlinx.android.synthetic.main.activity_protocol.*
import kotlinx.android.synthetic.main.layout_actionbar.*

/**
 * 用户隐私政策  https://devppsns.duluduludala.com/ppsns/protocol/privacyProtocol/v1.json   1
 * 用户协议 https://devppsns.duluduludala.com/ppsns/protocol/userProtocol/v1.json   2
 * 防骗子 /ppsns/protocol/bewareFraud/v1.json
 */
class ProtocolActivity : BaseActivity() {
    companion object {
        const val TYPE_PRIVACY_PROTOCOL = 1
        const val TYPE_USER_PROTOCOL = 2
        const val TYPE_OTHER = 3
    }

    private val type by lazy { intent.getIntExtra("type", TYPE_PRIVACY_PROTOCOL) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_protocol)
        initView()
    }

    private fun initView() {
        when (type) {
            TYPE_PRIVACY_PROTOCOL -> {
                hotT1.text = "隐私条款"
            }
            TYPE_USER_PROTOCOL -> {
                hotT1.text = "用户协议"
            }
        }

        btnBack.onClick {
            finish()
        }

        initWebview()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebview() {

//         //支持javascript web.getSettings().setJavaScriptEnabled(true);
//
//// 设置可以支持缩放 web.getSettings().setSupportZoom(true);
//
//// 设置出现缩放工具 web.getSettings().setBuiltInZoomControls(true);
//
////扩大比例的缩放 web.getSettings().setUseWideViewPort(true);
//
////自适应屏幕 web.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
//
//web.getSettings().setLoadWithOverviewMode(true);


        //支持javascript
        webView.settings.javaScriptEnabled = true
        // 设置可以支持缩放
        webView.settings.setSupportZoom(true)
        // 设置出现缩放工具
        webView.settings.builtInZoomControls = true
        //扩大比例的缩放
        webView.settings.useWideViewPort = true

        webView.settings.blockNetworkImage = false//解决图片不显示
        webView.settings.mixedContentMode =
            WebSettings.MIXED_CONTENT_ALWAYS_ALLOW//混合模式，允许https中加载http图片
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
        webView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                if (!title.isNullOrEmpty())
                    hotT1.text = title
            }
        }

        when (type) {
            TYPE_PRIVACY_PROTOCOL -> webView.loadUrl("${BaseConstant.SERVER_ADDRESS}protocol/privacyProtocol${Constants.END_BASE_URL}")
            TYPE_USER_PROTOCOL -> webView.loadUrl("${BaseConstant.SERVER_ADDRESS}protocol/userProtocol${Constants.END_BASE_URL}")
            TYPE_OTHER -> webView.loadUrl(intent.getStringExtra("url"))
        }


    }
}
