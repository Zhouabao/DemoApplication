package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.shuyu.gsyvideoplayer.GSYVideoManager
import kotlinx.android.synthetic.main.dialog_unlock_with_candy.*

/**
 *    author : ZFM
 *    date   : 2020/6/919:39
 *    desc   : 糖果解锁聊天
 *    version: 1.0
 */
class UnlockChatWithCandyDialog(val context1: Context) :
    Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_unlock_with_candy)
        initWindow()
        initView()

    }

    private fun initView() {
        closeBtn.clickWithTrigger {
            dismiss()
        }

        unlockBtn.clickWithTrigger {

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


    override fun dismiss() {
        super.dismiss()
        GSYVideoManager.releaseAllVideos()
    }
}