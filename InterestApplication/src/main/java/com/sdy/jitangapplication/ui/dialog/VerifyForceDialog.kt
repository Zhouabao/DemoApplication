package com.sdy.jitangapplication.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import androidx.core.view.isVisible
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_verify_force.*

/**
 *    author : ZFM
 *    date   : 2019/10/1614:30
 *    desc   :强制认证弹窗
 *    version: 1.0
 */
class VerifyForceDialog(val context1: Context, var status: Int = VIDEO_INTRODUCE_SUCCESS_MV) :
    Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_verify_force)
        initWindow()
        changeVerifyStatus(status)
    }

    //const SUCCESS_MV = 91;   // 视频通过
    //const FAIL_MV = 93;         // 视频拒绝
    companion object {
        const val VIDEO_INTRODUCE_SUCCESS_MV = 91
        const val VIDEO_INTRODUCE_FAIL_MV = 93
        const val VIDEO_INTRODUCE_GOING = 1//认证中

    }


    fun changeVerifyStatus(status: Int) {
        GlideUtil.loadCircleImg(context1, UserManager.getAvator(), userAvatar)
        when (status) {
            VIDEO_INTRODUCE_SUCCESS_MV -> {
                verifyIngAni.isVisible = false
                verifyIngAni.cancelAnimation()
                verifyStateBg.setImageResource(R.drawable.rectangle_oval_green_verify_pass)
                verifyStateLogo.setImageResource(R.drawable.icon_checked_relation)
                continueBtn.text = "继续使用"
                verifyState.text = "审核通过"
                verifyTip.text = "您已通过视频审核\n已开启私聊权限"

                continueBtn.clickWithTrigger {
                    dismiss()
                }
            }
            VIDEO_INTRODUCE_FAIL_MV -> {
                verifyIngAni.isVisible = false
                verifyIngAni.cancelAnimation()
                verifyStateBg.setImageResource(R.drawable.rectangle_red_green_verify_fail)
                verifyStateLogo.setImageResource(R.drawable.icon_delete)
                continueBtn.text = "重新认证"
                verifyState.text = "审核失败"
                verifyTip.text = "视频审核未通过\n您可以重新视频认证"
            }
            VIDEO_INTRODUCE_GOING -> {
                verifyIngAni.isVisible = true
                verifyIngAni.playAnimation()
                verifyStateBg.setImageResource(R.drawable.rectangle_red_green_verify_ing)
                verifyStateLogo.setImageResource(R.drawable.icon_wait_time)
                verifyState.text = "视频正在审核中"
                verifyTip.text = "视频正在审核中\n开启通知将在第一时间通知您"
                continueBtn.text = "好的"
                continueBtn.clickWithTrigger {
                    (context1 as Activity).finish()
                    dismiss()
                }
            }

        }
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.MATCH_PARENT
        params?.windowAnimations = R.style.MyDialogCenterAnimation
        window?.attributes = params
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setOnKeyListener { dialogInterface, keyCode, event ->
            keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0
        }
    }

    /**
     * 人工审核
     * 1 人工认证 2重传头像或则取消
     */
    fun humanVerify(type: Int) {
        val params = UserManager.getBaseParams()
        params["type"] = type
        RetrofitFactory.instance.create(Api::class.java)
            .humanAduit(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {

                }

                override fun onError(e: Throwable?) {

                }
            })

    }

}