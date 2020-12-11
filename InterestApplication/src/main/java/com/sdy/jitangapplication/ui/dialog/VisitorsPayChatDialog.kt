package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.VisitChatBean
import com.sdy.jitangapplication.ui.adapter.VisitPayChatAvatorAdater
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_visitors_pay_chat.*

/**
 *    author : ZFM
 *    date   : 2020/5/99:45
 *    desc   : 游客拉起付费弹窗
 *    version: 1.0
 */
class VisitorsPayChatDialog(val context1: Context) : Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_visitors_pay_chat)
        initWindow()
        initView()
        //获取搭讪的人员列表
        fakeChatupList()

    }

    private val adapter by lazy { VisitPayChatAvatorAdater() }
    private fun initView() {
        sugarsRv.layoutManager = LinearLayoutManager(context1, RecyclerView.HORIZONTAL, false)
        sugarsRv.adapter = adapter

        tobeVipBtn.clickWithTrigger {
            CommonFunction.startToFootPrice(context1)
        }

    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        params?.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(20F)
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation
        window?.attributes = params

    }

    override fun show() {
        super.show()
    }


    fun fakeChatupList() {
        RetrofitFactory.instance.create(Api::class.java)
            .fakeChatupList(UserManager.getSignParams())
            .excute(object :BaseSubscriber<BaseResp<ArrayList<VisitChatBean>>>(){
                override fun onStart() {
                    super.onStart()
                }
                override fun onNext(t: BaseResp<ArrayList<VisitChatBean>>) {
                    super.onNext(t)

                    adapter.setNewData(t.data)
                }

            })

    }


}