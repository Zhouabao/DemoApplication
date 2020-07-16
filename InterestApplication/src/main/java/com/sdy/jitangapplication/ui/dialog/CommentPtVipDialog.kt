package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.CloseDialogEvent
import com.sdy.jitangapplication.model.VipPowerBean
import com.sdy.jitangapplication.ui.activity.VipPowerActivity
import kotlinx.android.synthetic.main.dialog_open_pt_vip.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2020/5/99:45
 *    desc   :钻石会员评论
 *    version: 1.0
 */
class CommentPtVipDialog(
    val context1: Context
) :
    Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_pt_vip_comment)
        initWindow()
        initView()
        EventBus.getDefault().register(this)
    }

    private fun initView() {
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        openPtVipBtn.clickWithTrigger {
            context1.startActivity<VipPowerActivity>("type" to VipPowerBean.TYPE_PT_VIP)
        }

    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT

        window?.attributes = params

    }

    override fun show() {
        super.show()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCloseDialogEvent(event: CloseDialogEvent) {
        if (isShowing) {
            dismiss()
        }
    }


}