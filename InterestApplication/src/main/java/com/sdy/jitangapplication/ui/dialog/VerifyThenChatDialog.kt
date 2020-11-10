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
        const val FROM_CONTACT_VERIFY = 3
        const val FROM_APPLY_DATING = 4
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
                GlideUtil.loadCircleImg(context1, true, UserManager.getAvator(), accountDangerLogo)
                moreInfoTitle.text = context1.getString(R.string.verify_know)
                t2.text = context1.getString(R.string.verify_notice1)
                verifyBtn.text = context1.getString(R.string.ok_1)
                verifyBtn.setBackgroundResource(R.drawable.shape_rectangle_orange_24dp)
                closeBtn.isVisible = true
            }
            FROM_CHAT_VERIFY -> {//认证才能聊天
                closeBtn.isVisible = false
                accountDangerLogo.setImageResource(R.drawable.icon_verify_to_logo)
                moreInfoTitle.text = context1.getString(R.string.verify_then_chat)
                SpanUtils.with(t2)
                    .append(context1.getString(R.string.verify_then_chat_content))
                    .create()
                verifyBtn.text = context1.getString(R.string.verify_now)
                verifyBtn.setBackgroundResource(R.drawable.shape_rectangle_blue_24dp)
            }
            FROM_CONTACT_VERIFY -> {//认证才能解锁联系方式
                closeBtn.isVisible = false
                accountDangerLogo.setImageResource(R.drawable.icon_verify_then_contact)
                moreInfoTitle.text = context1.getString(R.string.verify_then_unlock)
                SpanUtils.with(t2)
                    .append(context1.getString(R.string.face_then_chat_to_real))
                    .create()
                verifyBtn.text = context1.getString(R.string.verify_now)
                verifyBtn.setBackgroundResource(R.drawable.shape_rectangle_blue_24dp)
            }
            FROM_APPLY_DATING -> {//认证才能解锁联系方式
                closeBtn.isVisible = false
                accountDangerLogo.setImageResource(R.drawable.icon_verify_to_logo)
                moreInfoTitle.text = context1.getString(R.string.verify_then_apply_dating)
                SpanUtils.with(t2)
                    .append(context1.getString(R.string.face_then_chat_to_real))
                    .create()
                verifyBtn.text = context1.getString(R.string.verify_now)
                verifyBtn.setBackgroundResource(R.drawable.shape_rectangle_blue_24dp)
            }
        }
        verifyBtn.onClick {
            when (type) {
                FROM_APPLY_DATING,
                FROM_CHAT_VERIFY,
                FROM_CONTACT_VERIFY -> {
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