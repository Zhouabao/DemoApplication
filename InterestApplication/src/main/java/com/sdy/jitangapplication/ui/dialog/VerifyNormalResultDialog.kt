package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.SizeUtils
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.ui.activity.VideoVerifyActivity
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_verify_normal_result.*

/**
 *    author : ZFM
 *    date   : 2019/10/1614:30
 *    desc   :普通认证结果弹窗
 *    version: 1.0
 */
class VerifyNormalResultDialog(val context1: Context, var status: Int = 0) :
    Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_verify_normal_result)
        initWindow()
        changeVerifyStatus(status)
    }


    companion object {
        const val VERIFY_NORMAL_NOTPASS_CHANGE_VIDEO = 93 //视频认证失败
        const val VERIFY_NORMAL_PASS = 91 //视频认证通过
    }


    fun changeVerifyStatus(status: Int) {
        GlideUtil.loadImg(context1, UserManager.getAvator(), userAvatar)
        when (status) {
            VERIFY_NORMAL_NOTPASS_CHANGE_VIDEO -> {
                verifyStateBg.setImageResource(R.drawable.rectangle_red_green_verify_fail)
                verifyStateLogo.setImageResource(R.drawable.icon_verify__not_pass)
                verifyState.text = "审核失败"
                verifyTip.text = "您的视频介绍未通过审核，您可在确保视频与头像一致的前提下重新录制"
                continueBtn.text = "重新录制"
                continueBtn.clickWithTrigger {
                    if (ActivityUtils.getTopActivity() !is VideoVerifyActivity)
                        VideoVerifyActivity.start(context1)
                    dismiss()
                }
            }
            VERIFY_NORMAL_PASS -> {
                verifyStateBg.setImageResource(R.drawable.rectangle_oval_green_verify_pass)
                verifyStateLogo.setImageResource(R.drawable.icon_checked_relation)
                verifyState.text = "审核通过"
                verifyTip.text = "您的视频介绍已通过审核"
                continueBtn.text = "继续使用"
                continueBtn.clickWithTrigger {
                    dismiss()
                }
            }

        }
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation
        params?.y = SizeUtils.dp2px(10F)
        window?.attributes = params
        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }
}