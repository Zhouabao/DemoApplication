package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.ext.onClick
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_verify_then_chat.*

/**
 *    author : ZFM
 *    date   : 2019/10/1614:30
 *    desc   :认证才能聊天弹窗
 *    version: 1.0
 */
class VerifyThenChatDialog(
    val context1: Context,
    var type: Int = FROM_CHAT_VERIFY
) : Dialog(context1, R.style.MyDialog) {
    companion object {
        const val FROM_VERIFY_MUST_KNOW = 1
        const val FROM_CHAT_VERIFY = 2
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_verify_then_chat)
        initWindow()
        initView()
    }


    fun initView() {
        when (type) {
            FROM_VERIFY_MUST_KNOW -> {//认证须知
                val parmas = accountDangerLogo.layoutParams as ConstraintLayout.LayoutParams
                parmas.width = SizeUtils.dp2px(86F)
                parmas.height = SizeUtils.dp2px(86F)
                accountDangerLogo.layoutParams = parmas
                GlideUtil.loadCircleImg(context1,true, UserManager.getAvator(), accountDangerLogo)
                moreInfoTitle.text = "认证须知"
                t2.text = "审核将与你的头像做校对，视频内容对会员用户公开为你的视频介绍，请点击录制键后介绍自己吧"
                verifyBtn.text = "好的"
                verifyBtn.setBackgroundResource(R.drawable.shape_rectangle_orange_24dp)
                closeBtn.isVisible = true
            }
            FROM_CHAT_VERIFY -> {//认证才能聊天
                closeBtn.isVisible = false
                accountDangerLogo.setImageResource(R.drawable.icon_verify_to_logo)
                moreInfoTitle.text = "认证后才能发起聊天"
                SpanUtils.with(t2)
                    .append("本平台为真人社交平台，为保护付费用户利益和真实性，您需要先进行人脸验证")
//                    .append("\n了解认证用户权益")
//                    .setClickSpan(object : ClickableSpan() {
//                        override fun onClick(widget: View) {
//                            CommonFunction.toast("这里是权益呢~~~")
//                        }
//
//                        override fun updateDrawState(ds: TextPaint) {
//                            super.updateDrawState(ds)
//                            ds.color = Color.parseColor("#6796FA")
//                            ds.isUnderlineText = false
//                        }
//                    })
//                    .setForegroundColor(Color.parseColor("#6796FA"))
                    .create()
                verifyBtn.text = "立即认证"
                verifyBtn.setBackgroundResource(R.drawable.shape_rectangle_blue_24dp)
            }
        }
        verifyBtn.onClick {
            when (type) {
                FROM_CHAT_VERIFY -> {
                    CommonFunction.startToFace(context1)
                    dismiss()
                }
                FROM_VERIFY_MUST_KNOW -> {
                    dismiss()
                }
            }

        }

        closeBtn.onClick {
            dismiss()
        }

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
    }

}