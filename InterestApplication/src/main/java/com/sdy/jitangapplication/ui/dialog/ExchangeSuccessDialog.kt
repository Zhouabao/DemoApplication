package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.ui.activity.MyOrderActivity
import kotlinx.android.synthetic.main.dialog_exchange_success.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/8/1513:59
 *    desc   : 兑换成功弹窗
 *    version: 1.0
 */
class ExchangeSuccessDialog(var context1: Context) : Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_exchange_success)
        initWindow()
        initview()
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        setCanceledOnTouchOutside(true)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation

        window?.attributes = params
    }


    fun initview() {
        confirmBtn.onClick {
            //            todo 应该要发送通知刷新界面
            dismiss()
        }

        seeOrder.onClick {
            //todo 跳转到我的订单
            context1.startActivity<MyOrderActivity>()
        }

    }

}