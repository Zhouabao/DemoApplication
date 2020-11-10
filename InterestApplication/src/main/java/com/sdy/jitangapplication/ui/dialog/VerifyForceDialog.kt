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
                continueBtn.text = context1.getString(R.string.continue_use)
                verifyState.text = context1.getString(R.string.mv_pass)
                verifyTip.text = context1.getString(R.string.mv_pass_to_chat)

                continueBtn.clickWithTrigger {
                    dismiss()
                }
            }
            VIDEO_INTRODUCE_FAIL_MV -> {
                verifyIngAni.isVisible = false
                verifyIngAni.cancelAnimation()
                verifyStateBg.setImageResource(R.drawable.rectangle_red_green_verify_fail)
                verifyStateLogo.setImageResource(R.drawable.icon_delete)
                continueBtn.text = context1.getString(R.string.mv_reverify)
                verifyState.text = context1.getString(R.string.verify_fail)
                verifyTip.text = context1.getString(R.string.mv_not_pass)
            }
            VIDEO_INTRODUCE_GOING -> {
//                verifyIngAni.isVisible = true
//                verifyIngAni.playAnimation()
                verifyStateBg.setImageResource(R.drawable.rectangle_red_green_verify_ing)
                verifyStateLogo.setImageResource(R.drawable.icon_wait_time)
                verifyState.text = context1.getString(R.string.mv_checking)
                verifyTip.text = context1.getString(R.string.my_checking_content)
                continueBtn.text = context1.getString(R.string.ok_1)
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