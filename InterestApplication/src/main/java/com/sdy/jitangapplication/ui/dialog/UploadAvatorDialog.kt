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
 *    date   : 2019/9/2316:45
 *    desc   : 上传头像的dialog
 *    version: 1.0
 */
class UploadAvatorDialog(context: Context) : Dialog(context, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_upload_avator)
        initWindow()
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
//        params?.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15F) * 2
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT

        params?.windowAnimations = R.style.MyDialogBottomAnimation
        params?.y = SizeUtils.dp2px(15F)
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
    }

}