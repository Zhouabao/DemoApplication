package com.example.demoapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.example.demoapplication.R
import kotlinx.android.synthetic.main.dialog_more_action.*

/**
 *    author : ZFM
 *    date   : 2019/6/2716:22
 *    desc   : 更多操作对话框
 *    version: 1.0
 */
class MoreActionDialog(context: Context, val from: String) : Dialog(context, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_more_action)
        initWindow()

        initView()
    }

    private fun initView() {
        if (from == "square") {
            llLahei.visibility = View.GONE
            llRemoveRelation.visibility = View.GONE
            llShare.visibility = View.GONE
        } else if (from == "square_detail") {
            llLahei.visibility = View.GONE
            llRemoveRelation.visibility = View.GONE
        } else {
            llCollect.visibility = View.GONE
            llShare.visibility = View.GONE
        }
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
    }


}