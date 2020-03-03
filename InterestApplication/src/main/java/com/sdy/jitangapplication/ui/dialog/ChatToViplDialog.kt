package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.dialog_greet_limit.*

/**
 *    author : ZFM
 *    date   : 2019/10/1614:30
 *    desc   : 聊天界面提示开会员弹窗
 *    version: 1.0
 */
class ChatToViplDialog(val context1: Context) : Dialog(context1, R.style.MyDialog) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_chat_to_vip)
        initWindow()
        initView()
    }

    private fun initView() {
        close.onClick {
            dismiss()
        }
        chargeBtn.onClick {
            ChargeVipDialog(ChargeVipDialog.INFINITE_CHAT, context1).show()
            dismiss()
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
            keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0
        }
    }
}