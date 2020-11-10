package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.CloseDialogEvent
import com.sdy.jitangapplication.ui.activity.VipPowerActivity
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_choiceness_open_pt_vip.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 1.开通黄金会员成为精选用户（男性）
 * 2.添加视频介绍成为精选用户（女性）
 */
class ChoicenessOpenPtVipDialog(val context1: Context) :
    Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_choiceness_open_pt_vip)
        initWindow()
        initChatData()
        EventBus.getDefault().register(this)
    }


    private fun initChatData() {
        GlideUtil.loadCircleImg(context1, UserManager.getAvator(), chatupAvator)

        // * 1.开通黄金会员成为精选用户（男性）
        // * 2.添加视频介绍成为精选用户（女性）
        if (UserManager.getGender() == 1) {
            choicenessTitle.text = context1.getString(R.string.tobe_choiceness_user)
            choicenessContent.text = context1.getString(R.string.tobe_gold_to_first_see_you)
            openPtVipBtn.text = context1.getString(R.string.open_gold_vip)
            openPtVipBtn.setBackgroundResource(R.drawable.gradient_gold_vip)
            openPtVipBtn.clickWithTrigger {
                CommonFunction.startToVip(context1, VipPowerActivity.SOURCE_MORE_EXPODE)
            }
        } else {
            choicenessTitle.text = context1.getString(R.string.tobe_choiceness_user)
            choicenessContent.text = context1.getString(R.string.upload_video_to_first_see_you)
            openPtVipBtn.text = context1.getString(R.string.add_video_introduce)
            openPtVipBtn.setBackgroundResource(R.drawable.gradient_orange_15_bottom)
            openPtVipBtn.clickWithTrigger {
                CommonFunction.startToVideoIntroduce(context1)
            }
        }

    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
//        window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation
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