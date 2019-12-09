package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.style.ClickableSpan
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.ui.activity.ProtocolActivity
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_privacy.*

/**
 *    author : ZFM
 *    date   : 2019/10/2519:15
 *    desc   :隐私协议弹窗
 *    version: 1.0
 */
class PrivacyDialog(val context1: Context) : Dialog(context1, R.style.MyDialog) {


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
        privacyContent.text = SpanUtils.with(privacyContent)
            .append("尊敬的用户，欢迎使用积糖。\n依据最新法律法规、监管政策等要求及业务实际情况，更新了")
            .append("《积糖用户协议》")
            .setClickSpan(clickSpanProtocol1)
            .setForegroundColor(context1.resources.getColor(R.color.colorOrange))
            .append("及")
            .setForegroundColor(context1.resources.getColor(R.color.color_333333))
            .append("《隐私政策》")
            .setClickSpan(clickSpanPrivacy1)
            .setForegroundColor(context1.resources.getColor(R.color.colorOrange))
            .append("，特此向您提示：\n\n")
            .setForegroundColor(context1.resources.getColor(R.color.color_333333))
            .append("请您务必仔细阅读并理解相关条款内容，在确认充分理解的前提下同意后使用积糖相关产品及服务。点击同意即代表您已经阅读并同意")
            .setForegroundColor(context1.resources.getColor(R.color.color_333333))
            .append("《积糖用户协议》")
            .setClickSpan(clickSpanProtocol)
            .setForegroundColor(context1.resources.getColor(R.color.colorOrange))
            .append("及")
            .setForegroundColor(context1.resources.getColor(R.color.color_333333))
            .append("《隐私政策》")
            .setClickSpan(clickSpanPrivacy)
            .setForegroundColor(context1.resources.getColor(R.color.colorOrange))
            .append("，如果您不同意，将可能影响使用积糖产品和服务。\n\n我们将按照法律法规要求，采取相应安全保护措施，尽力保护您的个人信息安全。")
            .setForegroundColor(context1.resources.getColor(R.color.color_333333))
            .create()

        agree.onClick {
            UserManager.saveAlertProtocol(true)
            dismiss()
        }
        disAgree.onClick {
            CommonFunction.toast("您需要同意《积糖用户协议》及《隐私政策》才能继续使用我们的产品及服务")
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
        setCanceledOnTouchOutside(false)
    }
}