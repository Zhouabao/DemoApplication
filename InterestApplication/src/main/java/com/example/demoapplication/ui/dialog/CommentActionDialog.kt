package com.example.demoapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.blankj.utilcode.util.SizeUtils
import com.example.demoapplication.R
import kotlinx.android.synthetic.main.dialog_comment_action.*

/**
 *    author : ZFM
 *    date   : 2019/6/2716:22
 *    desc   : 评论更多操作
 *    version: 1.0
 */
class CommentActionDialog(context: Context, val from: String) : Dialog(context, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_comment_action)
        initWindow()

        initView()
    }

    private fun initView() {
        if (from == "self") {
            jubaoComment.visibility = View.GONE
        } else {
            deleteComment.visibility = View.GONE
        }
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation
        params?.y = SizeUtils.dp2px(20F)

        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
    }


}