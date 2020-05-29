package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.blankj.utilcode.util.ActivityUtils
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.ui.activity.NewUserInfoSettingsActivity
import com.sdy.jitangapplication.ui.activity.VideoVerifyActivity
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_verify_normal_result.*
import org.jetbrains.anko.startActivity

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


    //const SUCCESS_MV = 91;   // 视频认证通过(普通)
//const FAIL_AVATAR = 92;  // 视频认证失败--去替换头像(普通)
//const FAIL_MV = 93;      // 视频认证失败----去替换视频(普通)
//
    companion object {
        const val VERIFY_NORMAL_NOTPASS_CHANGE_VIDEO = 93 //视频认证失败----去替换视频(普通)
        const val VERIFY_NORMAL_NOTPASS_CHANGE_AVATOR = 92 //视频认证失败--去替换头像(普通)
        const val VERIFY_NORMAL_PASS = 91 //视频认证通过
    }


    fun changeVerifyStatus(status: Int) {
        GlideUtil.loadImg(context1, UserManager.getAvator(), userAvatar)
        when (status) {
            VERIFY_NORMAL_NOTPASS_CHANGE_VIDEO -> {
                verifyState.text = "审核失败"
                verifyTip.text = "视频认证未通过您可以重新视频认证"
                continueBtn.text = "重新认证"
                continueBtn.clickWithTrigger {
                    if (ActivityUtils.getTopActivity() !is VideoVerifyActivity)
                        VideoVerifyActivity.start(context1)
                    else
                        dismiss()
                }
            }
            VERIFY_NORMAL_NOTPASS_CHANGE_AVATOR -> {
                verifyState.text = "审核失败"
                verifyTip.text = "视频认证未通过您可以替换真人头像重试"
                continueBtn.text = "替换头像"
                continueBtn.clickWithTrigger {
                    if (ActivityUtils.getTopActivity() !is NewUserInfoSettingsActivity)
                        context1.startActivity<NewUserInfoSettingsActivity>()
                    else
                        dismiss()
                }
            }
            VERIFY_NORMAL_PASS -> {
                verifyState.text = "审核通过"
                verifyTip.text = "您已通过视频认证已开启私聊权限"
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
        params?.width = WindowManager.LayoutParams.WRAP_CONTENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation
        window?.attributes = params
        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }
}