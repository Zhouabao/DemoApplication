package com.sdy.jitangapplication.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import kotlinx.android.synthetic.main.dialog_add_wish_gift.*

/**
 *    author : ZFM
 *    date   : 2019/8/1513:59
 *    desc   : 引导女性添加心愿礼物
 *    version: 1.0
 */
class AddWishGiftDialog(var context1: Context) : Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initWindow()
        setContentView(R.layout.dialog_add_wish_gift)
        initView()
    }

    private fun initView() {
        addWishGiftBtn.clickWithTrigger {
            dismiss()
        }
        backToMain.clickWithTrigger {
//            context1.startActivity<MainActivity>()
            (context1 as Activity).finish()
            dismiss()
        }
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        // 设置窗口背景透明度
//        params?.alpha = 0.5f
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.MATCH_PARENT
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(false)


    }


}