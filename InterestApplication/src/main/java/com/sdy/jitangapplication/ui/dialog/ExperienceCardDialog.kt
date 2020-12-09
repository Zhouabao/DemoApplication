package com.sdy.jitangapplication.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.ui.activity.RegisterInfoActivity
import kotlinx.android.synthetic.main.dialog_experience_card.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2020/5/99:45
 *    desc   : 黄金会员体验卡
 *    version: 1.0
 */
class ExperienceCardDialog(val context1: Context) : Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_experience_card)
        initWindow()
        initView()

    }

    private fun initView() {
        //todo 获取体验卡，接下来走注册流程 填写信息
        getCardBtn.clickWithTrigger {
            dismiss()
            ActivityUtils.finishActivity(context1 as Activity)
            context1.startActivity<RegisterInfoActivity>()
        }

    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        params?.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(20F)
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation
        window?.attributes = params

    }

    override fun show() {
        super.show()
    }


}