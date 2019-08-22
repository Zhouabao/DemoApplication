package com.example.demoapplication.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.example.demoapplication.R
import com.example.demoapplication.utils.UserManager
import com.kotlin.base.ext.onClick
import kotlinx.android.synthetic.main.tick_dialog_layout.*

/**
 *    author : ZFM
 *    date   : 2019/8/1513:59
 *    desc   :
 *    version: 1.0
 */
class TickDialog(var context1: Context) : Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initWindow()
        setContentView(R.layout.tick_dialog_layout)
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
        confirm.onClick {
            UserManager.startToLogin(context1 as Activity)
            dismiss()
        }
    }

}