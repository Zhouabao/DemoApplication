package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.blankj.utilcode.util.SizeUtils
import com.sdy.jitangapplication.R

/**
 *    author : ZFM
 *    date   : 2019/6/2716:22
 *    desc   : 更多操作对话框
 *    version: 1.0
 */
class ResolveBlackDialog(context: Context) : Dialog(context, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_resolve_black)
        initWindow()

    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.WRAP_CONTENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.y = SizeUtils.dp2px(20F)
        params?.windowAnimations = R.style.MyDialogBottomAnimation

        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
    }


}