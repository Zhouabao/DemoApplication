package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.ui.activity.IDVerifyActivity
import kotlinx.android.synthetic.main.dialog_human_verify.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/10/1614:30
 *    desc   :聊天界面人脸认证弹窗
 *    version: 1.0
 */
class HumanVerifyDialog(val context1: Context) :
    Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_human_verify)
        initWindow()
        initView()
    }


    fun initView() {
        verifyBtn.onClick {
            context1.startActivity<IDVerifyActivity>("face_source_type" to 1)
            dismiss()
        }

        closeBtn.onClick {
            dismiss()
        }
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