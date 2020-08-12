package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Gravity
import android.view.WindowManager
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.ui.activity.ChooseDatingTypeActivity
import kotlinx.android.synthetic.main.dialog_publish_dating_must_know.*
import org.jetbrains.anko.startActivity

/**
 * 发布约会须知
 */
class PublishDatingKnowDialog(val context1: Context) : Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_publish_dating_must_know)
        initWindow()
        initView()
    }


    override fun show() {
        super.show()
//        timer.start()
    }

    override fun dismiss() {
        super.dismiss()
//        timer.cancel()
    }

    /** 倒计时60秒，一次1秒 */
    val timer = object : CountDownTimer(6 * 1000, 1000) {
        override fun onFinish() {
            knowBtn.text = "我已了解"
            knowBtn.setBackgroundResource(R.drawable.gradient_orange_15_bottom)
            knowBtn.isEnabled = true
        }

        override fun onTick(p0: Long) {
            knowBtn.text = "我已了解（${p0 / 1000}s）"
        }

    }

    private fun initView() {
        knowBtn.isEnabled = true


        knowBtn.clickWithTrigger {
            //todo  跳转到发布约会
            context1.startActivity<ChooseDatingTypeActivity>()
            dismiss()
        }
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
//        window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation
        window?.attributes = params
        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }


}