package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.ui.activity.NewUserInfoSettingsActivity
import kotlinx.android.synthetic.main.dialog_right_slide_out.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/11/49:45
 *    desc   : 右滑次数用尽dialog
 *    version: 1.0
 */
class RightSlideOutdDialog(val myContext: Context) : Dialog(myContext, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_right_slide_out)
        initWindow()
        initView()
    }

    private fun initView() {
        slideOutBtn.onClick {
            myContext.startActivity<NewUserInfoSettingsActivity>()
        }
        slideOutClose.onClick {
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
        setCanceledOnTouchOutside(true)
    }
}