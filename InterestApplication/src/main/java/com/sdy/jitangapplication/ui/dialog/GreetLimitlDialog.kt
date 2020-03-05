package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import com.kotlin.base.ext.onClick
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.dialog_greet_limit.*

/**
 *    author : ZFM
 *    date   : 2019/10/1614:30
 *    desc   : 对方用户招呼上限
 *    version: 1.0
 */
class GreetLimitlDialog(val context1: Context, var targetAvator: String = "") : Dialog(context1, R.style.MyDialog) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_greet_limit)
        initWindow()
        initView()
    }

    private fun initView() {
        close.onClick {
            dismiss()
        }
        chargeBtn.onClick {
            ChargeVipDialog(ChargeVipDialog.DOUBLE_HI, context1).show()
            dismiss()
        }

        GlideUtil.loadCircleImg(context1, targetAvator, targetUserAvator)
    }

    override fun dismiss() {
        super.dismiss()
    }


    override fun show() {
        super.show()
        lottieGeetLimit.playAnimation()
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