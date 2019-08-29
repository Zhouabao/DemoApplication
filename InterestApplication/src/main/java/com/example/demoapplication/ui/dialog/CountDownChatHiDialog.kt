package com.example.demoapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.WindowManager
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.utils.UriUtils
import com.example.demoapplication.utils.UserManager
import com.kotlin.base.ext.onClick
import kotlinx.android.synthetic.main.countdown_chat_hi_dialog_layout.*

/**
 *    author : ZFM
 *    date   : 2019/8/1513:59
 *    desc   :补充次数的倒计时
 *    version: 1.0
 */
class CountDownChatHiDialog(var context1: Context) : Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.countdown_chat_hi_dialog_layout)
        initWindow()
        initview()
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        // 设置窗口背景透明度
//        params?.alpha = 0.5f
//         android:layout_marginLeft="15dp"
//        android:layout_marginRight="15dp"
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
    }

    fun initview() {
        confirm.onClick {
            dismiss()
        }

        GlideUtil.loadCircleImg(context1, UserManager.getAvator(), myAvator)

        object : CountDownTimer(UserManager.getCountDownTimet() * 1000L, 1000L) {
            override fun onFinish() {
                dismiss()
            }

            override fun onTick(millisUntilFinished: Long) {
                fullTime.text = "${UriUtils.ms2HMS((millisUntilFinished).toInt())}后补充"
//                fullTime.text = "请等待充能"
            }

        }.start()

    }

}