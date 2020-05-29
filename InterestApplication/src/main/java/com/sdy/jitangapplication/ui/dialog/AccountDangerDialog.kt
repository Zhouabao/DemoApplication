package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ActivityUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.ui.activity.IDVerifyActivity
import com.sdy.jitangapplication.ui.activity.NewUserInfoSettingsActivity
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_account_danger.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/10/1614:30
 *    desc   :账号异常弹窗
 *    version: 1.0
 */
class AccountDangerDialog(val context1: Context, var status: Int = VERIFY_NEED_ACCOUNT_DANGER) :
    Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_account_danger)
        initWindow()
        changeVerifyStatus(status)
    }


    companion object {
        const val VERIFY_NEED_AVATOR_INVALID = -1 //头像审核不通过去认证
        const val VERIFY_NEED_ACCOUNT_DANGER = 0 //账号异常去认证
        const val VERIFY_ING = 1//认证中
        const val VERIFY_NOT_PASS = 2//未通过
        const val VERIFY_PASS = 3//通过
    }


    fun changeVerifyStatus(status: Int) {
        when (status) {
            VERIFY_NEED_AVATOR_INVALID -> {
                accountDangerLogo.isVisible = false
                accountDangerImgAlert.isVisible = true
                humanVerify.isVisible = false
                accountDangerVerifyStatuLogo.isVisible = true
                GlideUtil.loadImg(context1, UserManager.getAvator(), accountDangerVerifyStatuLogo)
                accountDangerTitle.text = "人脸认证"
                accountDangerContent.text = "请进行人脸认证\n以确保头像为真人"
                accountDangerBtn.text = "去认证"
                accountDangerLoading.isVisible = false
                accountDangerBtn.isEnabled = true
                accountDangerBtn.onClick {
                    CommonFunction.startToFace(context1)
                }
            }
            VERIFY_NEED_ACCOUNT_DANGER -> {
                accountDangerImgAlert.isVisible = false
                humanVerify.isVisible = false
                accountDangerLogo.isVisible = true
                accountDangerVerifyStatuLogo.isVisible = false
                accountDangerTitle.text = "账号异常"
                accountDangerContent.text = "您的账号存在高危风险异常\n请完成认证解除异常"
                accountDangerBtn.text = "去认证"
                accountDangerLoading.isVisible = false
                accountDangerBtn.isEnabled = true
                accountDangerBtn.onClick {
                    CommonFunction.startToFace(context1,IDVerifyActivity.TYPE_ACCOUNT_DANGER)
                }
            }
            VERIFY_ING -> {
                accountDangerImgAlert.isVisible = false
                humanVerify.isVisible = false
                accountDangerLogo.isVisible = false
                accountDangerVerifyStatuLogo.isVisible = true
                accountDangerVerifyStatuLogo.setImageResource(R.drawable.icon_verify_account_ing)
                accountDangerTitle.text = "认证审核中"
                accountDangerContent.text = "认证审核通过后将为您解冻账号\n请耐心等待"
                accountDangerBtn.text = ""
                accountDangerLoading.isVisible = true
                accountDangerBtn.isEnabled = false

            }
            VERIFY_NOT_PASS -> {
                accountDangerImgAlert.isVisible = false
                humanVerify.isVisible = false
                accountDangerLogo.isVisible = false
                accountDangerVerifyStatuLogo.isVisible = true
                accountDangerVerifyStatuLogo.setImageResource(R.drawable.icon_verify_account_not_pass)
                accountDangerTitle.text = "认证审核不通过"
                accountDangerContent.text = "您当前头像无法通过人脸对比\n请更换本人头像重新进行认证审核"
                accountDangerBtn.text = "修改头像"
                accountDangerLoading.isVisible = false
                accountDangerBtn.isEnabled = true
                accountDangerBtn.onClick {
                    if (ActivityUtils.getTopActivity() !is NewUserInfoSettingsActivity)
                        context1.startActivity<NewUserInfoSettingsActivity>()
                    else
                        dismiss()
                }
                humanVerify.onClick {
                    humanVerify(1)
                }
            }
            VERIFY_PASS -> {
                accountDangerImgAlert.isVisible = false
                humanVerify.isVisible = false
                accountDangerLogo.isVisible = false
                accountDangerVerifyStatuLogo.isVisible = true
                accountDangerVerifyStatuLogo.setImageResource(R.drawable.icon_verify_account_pass)
                accountDangerTitle.text = "认证审核成功"
                accountDangerContent.text = "您已通过认证审核\n已为您解冻账号，现在可按正常流程操作"
                accountDangerBtn.text = "知道了"
                accountDangerLoading.isVisible = false
                accountDangerBtn.isEnabled = true
                accountDangerBtn.onClick {
                    dismiss()
                }
            }
        }
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.WRAP_CONTENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
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