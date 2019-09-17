package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.sdy.jitangapplication.R
/**
 *    author : ZFM
 *    date   : 2019/9/1715:52
 *    desc   : 替换头像的dialog
 *    version: 1.0
 */
class ChangeAvatorDialog (var context1: Context) : Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_avator_dialog_layout)
        initWindow()
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        // 设置窗口背景透明度
//        params?.alpha = 0.5f
//         android:layout_marginLeft="15dp"
//        android:layout_marginRight="15dp"
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(false)
    }

}