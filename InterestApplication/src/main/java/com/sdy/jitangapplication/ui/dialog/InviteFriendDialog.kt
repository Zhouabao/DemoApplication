package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.ui.activity.InviteRewardsActivity
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_invite_friend.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2020/7/2811:05
 *    desc   : 邀请好友分享
 *    version: 1.0
 */
class InviteFriendDialog(val context1: Context) : Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_invite_friend)
        initWindow()

        initView()
    }

    private fun initView() {
        receiveRedPacket.clickWithTrigger {
            context1.startActivity<InviteRewardsActivity>()
        }
        closeBtn.clickWithTrigger {
            dismiss()
        }
    }

    override fun dismiss() {
        super.dismiss()
        UserManager.showCompleteUserCenterDialog = true

    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.WRAP_CONTENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT

        params?.windowAnimations = R.style.MyDialogCenterAnimation
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }
}