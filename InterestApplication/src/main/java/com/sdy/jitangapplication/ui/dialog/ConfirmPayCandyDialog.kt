package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.dialog_confirm_recharge_candy.*


/**
 *    author : ZFM
 *    date   : 2019/6/2716:22
 *    desc   : 确认糖果支付按钮
 *    version: 1.0
 */
class ConfirmPayCandyDialog(val myContext: Context) : Dialog(myContext, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_confirm_recharge_candy)
        initWindow()
        initView()
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogCenterAnimation

        window?.attributes = params
    }


    private fun initView() {
        price.typeface = Typeface.createFromAsset(myContext.assets, "DIN_Alternate_Bold.ttf")
        close.onClick {
            dismiss()
        }
        wechatCl.onClick {
            wechatCheck.isChecked = true
            alipayCheck.isChecked = false
        }
        alipayCl.onClick {
            alipayCheck.isChecked = true
            wechatCheck.isChecked = false
        }
    }

}