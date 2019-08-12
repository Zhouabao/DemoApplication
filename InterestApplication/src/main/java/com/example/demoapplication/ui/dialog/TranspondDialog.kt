package com.example.demoapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.blankj.utilcode.util.ToastUtils
import com.example.demoapplication.R
import com.example.demoapplication.model.SquareBean
import com.example.demoapplication.ui.activity.ContactBookActivity
import kotlinx.android.synthetic.main.dialog_transpond.*

/**
 *    author : ZFM
 *    date   : 2019/6/2716:22
 *    desc   : 转发动态对话框
 *    version: 1.0
 */
class TranspondDialog(context: Context, var squareBean: SquareBean? = null) : Dialog(context, R.style.MyDialog),
    View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_transpond)
        initWindow()
        initView()
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = params
    }


    private var position: Int = -1
    private fun initView() {
        transpondFriend.setOnClickListener(this)
        transpondWechat.setOnClickListener(this)
        transpondWechatZone.setOnClickListener(this)
        transpondWebo.setOnClickListener(this)
        transpondQQ.setOnClickListener(this)
        transpondQQZone.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.transpondFriend -> {//转发到好友
                if (squareBean != null) {
                    ContactBookActivity.start(context, squareBean!!)
                    dismiss()
                }

                ToastUtils.showShort(transpondFriend.text.toString())
            }
            R.id.transpondWebo -> {
                ToastUtils.showShort(transpondWebo.text.toString())

            }
            R.id.transpondWechat -> {
                ToastUtils.showShort(transpondWechat.text.toString())

            }
            R.id.transpondWechatZone -> {
                ToastUtils.showShort(transpondWechatZone.text.toString())

            }
            R.id.transpondQQ -> {
                ToastUtils.showShort(transpondQQ.text.toString())

            }
            R.id.transpondQQZone -> {
                ToastUtils.showShort(transpondQQZone.text.toString())

            }
        }

    }

    private fun sendTransPondMessage() {

    }


}