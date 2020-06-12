package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import kotlinx.android.synthetic.main.dialog_video_add_time.*

/**
 *    author : ZFM
 *    date   : 2020/5/1910:12
 *    desc   :视频认证弹窗
 *    version: 1.0
 */
class VideoAddChatTimeDialog(val myContext: Context) :
    Dialog(myContext, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_video_add_time)
        initWindow()
        initView()
    }


    private fun initView() {
        addVideoBtn.clickWithTrigger {
            CommonFunction.startToVideoIntroduce(myContext)
            dismiss()
        }
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        // 设置窗口背景透明度
//        params?.alpha = 0.5f
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
//        params?.y = SizeUtils.dp2px(10F)
        params?.windowAnimations = R.style.MyDialogBottomAnimation
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
        setCancelable(true)
    }


}