package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.ShowGuideChangeStyleEvent
import kotlinx.android.synthetic.main.dialog_publish_dating.*
import org.greenrobot.eventbus.EventBus

/**
 *    author : ZFM
 *    date   : 2019/10/1614:30
 *    desc   : 发布出游活动
 *    version: 1.0
 */
class PublishDatingDialog(val context1: Context) : Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_publish_dating)
        initWindow()
        initView()
    }

    private fun initView() {
        close.onClick {
            dismiss()
        }

        publishDatingBtn.clickWithTrigger {
            CommonFunction.checkPublishDating(context1)
            dismiss()
        }

    }

    override fun dismiss() {
        super.dismiss()
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.WRAP_CONTENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogCenterAnimation
        window?.attributes = params

    }


}