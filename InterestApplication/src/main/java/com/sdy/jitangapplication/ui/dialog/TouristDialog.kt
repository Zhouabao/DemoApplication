package com.sdy.jitangapplication.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_tourist.*

/**
 *    author : ZFM
 *    date   : 2019/9/2316:45
 *    desc   : 提醒游客登录
 *    version: 1.0
 */
class TouristDialog(val context1: Context) : Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_tourist)
        initWindow()
        initView()
    }

    private fun initView() {
        loginOrRegBtn.clickWithTrigger {
            UserManager.startToLogin(context1 as Activity)
            dismiss()
        }
        touristCount.text = "此为注册用户功能\n加入积糖，与${UserManager.registerFileBean?.people_amount?: 0}位糖宝达成联系"
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
//        params?.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15F) * 2
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT

        params?.windowAnimations = R.style.MyDialogBottomAnimation
//        params?.y = SizeUtils.dp2px(15F)
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
    }

}