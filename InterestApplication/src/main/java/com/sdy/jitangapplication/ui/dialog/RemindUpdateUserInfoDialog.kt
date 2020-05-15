package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.AnswerBean
import com.sdy.jitangapplication.ui.adapter.MoreInfoAdapter
import kotlinx.android.synthetic.main.activity_new_user_info_settings.*

/**
 *    author : ZFM
 *    date   : 2019/8/1513:59
 *    desc   : 兑换成功弹窗
 *    version: 1.0
 */
class RemindUpdateUserInfoDialog(var context1: Context, val answerList: MutableList<AnswerBean>) :
    Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_remind_update_user_info)
        initWindow()
        initview()
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        setCanceledOnTouchOutside(false)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.MATCH_PARENT
//        params?.windowAnimations = R.style.MyDialogBottomAnimation

        window?.attributes = params
    }

    private val moreInfoAdapter by lazy { MoreInfoAdapter() }

    fun initview() {
        //更多信息
        rvMoreInfo.layoutManager = LinearLayoutManager(context1, RecyclerView.VERTICAL, false)
        rvMoreInfo.adapter = moreInfoAdapter
        moreInfoAdapter.setNewData(answerList)
    }

    override fun show() {
        super.show()
        rvMoreInfo.postDelayed({ dismiss() }, 3000L)
    }

}