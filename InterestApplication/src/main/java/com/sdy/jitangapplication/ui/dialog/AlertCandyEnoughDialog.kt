package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.customer_alert_dialog_layout.cancel
import kotlinx.android.synthetic.main.customer_alert_dialog_layout.confirm
import kotlinx.android.synthetic.main.dialog_alert_candy_enough_layout.*

/**
 *    author : ZFM
 *    date   : 2019/8/1513:59
 *    desc   :
 *    version: 1.0
 */
class AlertCandyEnoughDialog(var context1: Context, var from: Int = FROM_PRODUCT) :
    Dialog(context1, R.style.MyDialog) {
    companion object {
        val FROM_PRODUCT = 1
        val FROM_SEND_GIFT = 2
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initWindow()
        setContentView(R.layout.dialog_alert_candy_enough_layout)
        initview()
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        // 设置窗口背景透明度
//        params?.alpha = 0.5f
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.MATCH_PARENT
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(false)
    }

    fun initview() {
        t2.text = if (from == FROM_PRODUCT) {
            "您账户内糖果不足，暂不能兑换该商品，\n您也可以尝试充值后再试。"
        } else {
            "您账户内糖果不足，请充值后再试"
        }
        cancel.onClick {
            dismiss()
        }

        confirm.onClick {
            RechargeCandyDialog(context1).show()
            dismiss()
        }

    }

}