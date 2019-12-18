package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.kotlin.base.ext.onClick
import com.sdy.baselibrary.utils.CustomClickListener
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.event.ShowCompleteLabelEvent
import com.sdy.jitangapplication.ui.activity.AddLabelActivity
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_complete_label.*
import kotlinx.android.synthetic.main.dialog_harassment.harassmentClose
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/10/1614:30
 *    desc   : 完善兴趣提示
 *    version: 1.0
 */
class CompleteLabelDialog(val context1: Context) : Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_complete_label)
        initWindow()
        initView()
    }

    private fun initView() {
        harassmentClose.onClick {
            dismiss()
        }
        completeLabelBtn.onClick(object : CustomClickListener() {
            override fun onSingleClick(view: View) {
                context1.startActivity<AddLabelActivity>("from" to AddLabelActivity.FROM_ADD_NEW)
            }

        })
    }

    override fun dismiss() {
        super.dismiss()
        EventBus.getDefault().post(ShowCompleteLabelEvent(true))
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.WRAP_CONTENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogCenterAnimation
        window?.attributes = params
        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }


}