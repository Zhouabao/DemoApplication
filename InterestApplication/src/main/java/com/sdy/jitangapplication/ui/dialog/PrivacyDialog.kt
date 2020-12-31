package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.style.ClickableSpan
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.ui.activity.ProtocolActivity
import com.sdy.jitangapplication.ui.activity.SplashActivity
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_privacy.*

/**
 *    author : ZFM
 *    date   : 2019/10/2519:15
 *    desc   :隐私协议弹窗
 *    version: 1.0
 */
class PrivacyDialog(val context1: Context) :
    Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_privacy)

        initWindow()
        initView()
    }

    private fun initView() {

        val clickSpanPrivacy = object : ClickableSpan() {
            override fun onClick(p0: View) {
                val intent = Intent(context1, ProtocolActivity::class.java)
                intent.putExtra("type", ProtocolActivity.TYPE_PRIVACY_PROTOCOL)
                context1.startActivity(intent)
            }

        }

        val clickSpanProtocol = object : ClickableSpan() {
            override fun onClick(p0: View) {
                val intent = Intent(context1, ProtocolActivity::class.java)
                intent.putExtra("type", ProtocolActivity.TYPE_USER_PROTOCOL)
                context1.startActivity(intent)
            }

        }
        val clickSpanPrivacy1 = object : ClickableSpan() {
            override fun onClick(p0: View) {
                val intent = Intent(context1, ProtocolActivity::class.java)
                intent.putExtra("type", ProtocolActivity.TYPE_PRIVACY_PROTOCOL)
                context1.startActivity(intent)
            }

        }

        val clickSpanProtocol1 = object : ClickableSpan() {
            override fun onClick(p0: View) {
                val intent = Intent(context1, ProtocolActivity::class.java)
                intent.putExtra("type", ProtocolActivity.TYPE_USER_PROTOCOL)
                context1.startActivity(intent)
            }

        }
        privacyContent.highlightColor = context1.resources.getColor(R.color.colorTransparent)
        SpanUtils.with(privacyContent)
            .append(context1.getString(R.string.privacy_t1))
            .append("《${context1.resources.getString(R.string.user_protocol)}》")
            .setClickSpan(clickSpanProtocol1)
            .setForegroundColor(context1.resources.getColor(R.color.colorOrange))
            .append(context1.getString(R.string.login_tip_and))
            .setForegroundColor(context1.resources.getColor(R.color.color_333333))
            .append("《${context1.resources.getString(R.string.privacy_protocol)}》")
            .setClickSpan(clickSpanPrivacy1)
            .setForegroundColor(context1.resources.getColor(R.color.colorOrange))
            .append(context1.getString(R.string.privacy_t2))
            .setForegroundColor(context1.resources.getColor(R.color.color_333333))
            .append(context1.getString(R.string.privacy_t3))
            .setForegroundColor(context1.resources.getColor(R.color.color_333333))
            .append(context1.getString(R.string.privacy_t7))
            .setForegroundColor(context1.resources.getColor(R.color.colorOrange))
            .append(context1.getString(R.string.privacy_t8))
            .setForegroundColor(context1.resources.getColor(R.color.color_333333))
            .append("《${context1.resources.getString(R.string.user_protocol)}》")
            .setClickSpan(clickSpanProtocol)
            .setForegroundColor(context1.resources.getColor(R.color.colorOrange))
            .append(context1.getString(R.string.login_tip_and))
            .setForegroundColor(context1.resources.getColor(R.color.color_333333))
            .append("《${context1.resources.getString(R.string.privacy_protocol)}》")
            .setClickSpan(clickSpanPrivacy)
            .setForegroundColor(context1.resources.getColor(R.color.colorOrange))
            .append(context1.getString(R.string.privacy_t4))
            .setForegroundColor(context1.resources.getColor(R.color.color_333333))
            .create()

//        agree.onClick {
//            UserManager.saveAlertProtocol(true)
//            dismiss()
//            (context1 as SplashActivity).initPermissionsAndlogin()
//        }
        disAgree.onClick {
            CommonFunction.toast(
                context1.getString(R.string.privacy_t5)
                        + "${context1.resources.getString(R.string.user_protocol)}》"
                        + context1.getString(R.string.login_tip_and)
                        + "《${context1.resources.getString(R.string.privacy_protocol)}"
                        + context1.getString(R.string.privacy_t6)
            )
        }

    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        // 设置窗口背景透明度
//        params?.alpha = 0.5f
//         android:layout_marginLeft="15dp"
//        android:layout_marginRight="15dp"
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = params
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setOnKeyListener { dialogInterface, keyCode, event ->
            keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0
        }
    }

    override fun dismiss() {
        super.dismiss()
    }


}