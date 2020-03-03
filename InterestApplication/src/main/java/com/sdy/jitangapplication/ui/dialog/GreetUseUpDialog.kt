package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.GreetTimesBean
import com.sdy.jitangapplication.ui.activity.IDVerifyActivity
import kotlinx.android.synthetic.main.dialog_greet_use_up.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/10/1614:30
 *    desc   : 招呼次数用尽弹窗
 *    version: 1.0
 */
class GreetUseUpDialog(val context1: Context, val type: Int, var greetTimesBean: GreetTimesBean? = null) :
    Dialog(context1, R.style.MyDialog) {


    companion object {
        const val GREET_USE_UP_CHARGEVIP = 0 //招呼次数用尽去充值VIP
        const val GREET_USE_UP_VERIFY = 1//招呼次数用尽去认证获取次数
        const val GREET_USE_UP_TOMORROW = 2//招呼次数用尽,明天再来
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_greet_use_up)
        initWindow()
        initView()
    }

    private fun initView() {
        when (type) {
            GREET_USE_UP_VERIFY -> {
                normalGreetTimes.text = "普通用户招呼次数+${greetTimesBean?.normal_cnt}"
                verifyGreetTimes.text = "真人认证招呼次数+${greetTimesBean?.isfaced}"
                vipGreetTimes.text = "开通会员招呼次数+${greetTimesBean?.normal_cnt}"

                bg.setImageResource(R.drawable.icon_bg_greet_use_up_verify_big)
                greetUseUpContent.text = SpanUtils.with(greetUseUpContent)
                    .append("为鼓励用户真实性\n通过认证的用户每天可多打")
                    .append("${greetTimesBean?.isfaced}")
                    .setForegroundColor(context1.resources.getColor(R.color.colorOrange))
                    .append("个招呼")
                    .create()
                verifyOrVipLl.isVisible = true
                greetUseUpBtn.text = "立即认证"
            }
            GREET_USE_UP_CHARGEVIP -> {
                normalGreetTimes.text = "普通用户招呼次数+${greetTimesBean?.normal_cnt}"
                verifyGreetTimes.text = "真人认证招呼次数+${greetTimesBean?.isfaced}"
                vipGreetTimes.text = "开通会员招呼次数+${greetTimesBean?.normal_cnt}"

                bg.setImageResource(R.drawable.icon_bg_greet_use_up_verify_big)
                greetUseUpContent.text = SpanUtils.with(greetUseUpContent)
                    .append("充值会员每天可多打")
                    .append("${greetTimesBean?.isvip}")
                    .setForegroundColor(context1.resources.getColor(R.color.colorOrange))
                    .append("个招呼\n马上开通吧")
                    .create()
                verifyOrVipLl.isVisible = true
                greetUseUpBtn.text = "开通会员"
            }
            GREET_USE_UP_TOMORROW -> {
                bg.setImageResource(R.drawable.icon_bg_greet_use_up_verify)
                greetUseUpContent.text = "您的招呼次数已用完\n明天再来看看吧！"
                verifyOrVipLl.isVisible = false
                greetUseUpBtn.text = "好的"
            }
        }

        close.onClick {
            dismiss()
        }
        greetUseUpBtn.onClick {
            when (type) {
                GREET_USE_UP_CHARGEVIP -> {
                    greetUseUpContent.text = SpanUtils.with(greetUseUpContent)
                        .append("为鼓励用户真实性通过认证的用户每天可多打")
                        .append("2")
                        .setForegroundColor(context1.resources.getColor(R.color.colorOrange))
                        .append("个招呼")
                        .create()
                    ChargeVipDialog(ChargeVipDialog.DOUBLE_HI, context1, ChargeVipDialog.PURCHASE_VIP).show()
                    dismiss()
                }
                GREET_USE_UP_VERIFY -> {
                    greetUseUpContent.text = SpanUtils.with(greetUseUpContent)
                        .append("充值会员每天可多打")
                        .append("2")
                        .setForegroundColor(context1.resources.getColor(R.color.colorOrange))
                        .append("个招呼")
                        .create()
                    context1.startActivity<IDVerifyActivity>()
                    dismiss()
                }
                GREET_USE_UP_TOMORROW -> {
                    greetUseUpContent.text = "您的招呼次数已用完\n明天再来看看吧！"
                    dismiss()
                }
            }
        }
    }

    override fun dismiss() {
        super.dismiss()
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.WRAP_CONTENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogCenterAnimation
        window?.attributes = params
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        setOnKeyListener { dialogInterface, keyCode, event ->
            false
        }
    }
}