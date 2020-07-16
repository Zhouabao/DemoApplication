package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.SpanUtils
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.CloseDialogEvent
import com.sdy.jitangapplication.event.MatchByWishHelpEvent
import com.sdy.jitangapplication.model.ChatUpBean
import com.sdy.jitangapplication.model.VipPowerBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.activity.VipPowerActivity
import kotlinx.android.synthetic.main.dialog_video_open_pt_vip.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2020/5/99:45
 *    desc   :钻石解锁联系方式
 */
class VideoOpenPtVipDialog(val context1: Context) :
    Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_video_open_pt_vip)
        initWindow()
        initView()
        EventBus.getDefault().register(this)
    }

    private fun initView() {
        openPtVipBtn.clickWithTrigger {
            context1.startActivity<VipPowerActivity>("type" to VipPowerBean.TYPE_PT_VIP)
        }
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
//        window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = params
        setCancelable(true)
        setCanceledOnTouchOutside(true)
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