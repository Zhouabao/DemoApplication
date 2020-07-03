package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.blankj.utilcode.util.SizeUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.ui.activity.ChangeUserContactActivity
import kotlinx.android.synthetic.main.dialog_contact_pass.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/10/1614:30
 *    desc   :联系方式审核未通过
 *    version: 1.0
 */
class ContactNotPassDialog(val context1: Context) :
    Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_contact_pass)
        initWindow()
        changeVerifyStatus()
    }


    fun changeVerifyStatus() {

        //关闭弹窗
        closeBtn.clickWithTrigger {
            dismiss()
        }


        //立即替换
        changeBtn.clickWithTrigger {
            context1.startActivity<ChangeUserContactActivity>()
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