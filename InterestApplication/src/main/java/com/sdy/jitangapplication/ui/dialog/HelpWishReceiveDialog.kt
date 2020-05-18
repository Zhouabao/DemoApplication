package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.dialog_help_wish_receive.*

/**
 *    author : ZFM
 *    date   : 2020/4/29:36
 *    desc   : 助力领取
 *    version: 1.0
 */
class HelpWishReceiveDialog(var get_help_amount: Int, context: Context) :
    Dialog(context, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_help_wish_receive)
        initWindow()
        initView()
    }

    private fun initView() {
        receivedCandyAmount.text = "糖果+${get_help_amount}"
    }

    override fun show() {
        super.show()
        receivedCandyAmount.postDelayed({
            dismiss()
        },1500L)
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT

        params?.windowAnimations = R.style.MyDialogCenterAnimation
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
        setCancelable(true)
    }

}