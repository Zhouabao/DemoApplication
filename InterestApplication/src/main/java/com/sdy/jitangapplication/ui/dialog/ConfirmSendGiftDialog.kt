package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.GiftBean
import kotlinx.android.synthetic.main.customer_alert_dialog_layout.cancel
import kotlinx.android.synthetic.main.customer_alert_dialog_layout.confirm
import kotlinx.android.synthetic.main.dialog_alert_candy_enough_layout.*

/**
 *    author : ZFM
 *    date   : 2019/8/1513:59
 *    desc   :确定赠送糖果
 *    version: 1.0
 */
class ConfirmSendGiftDialog(var context1: Context, val giftName: GiftBean) :
    Dialog(context1, R.style.MyDialog) {
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
        t2.text = "你确定赠送「${giftName.title}」吗？"
        confirm.text = "赠送礼物"
        cancel.onClick {
            dismiss()
        }

        confirm.onClick {
            CommonFunction.toast("赠送礼物")
            dismiss()
        }

    }

}