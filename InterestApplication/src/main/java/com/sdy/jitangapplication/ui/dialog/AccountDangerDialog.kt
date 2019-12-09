package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.isVisible
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.ui.activity.IDVerifyActivity
import kotlinx.android.synthetic.main.dialog_account_danger.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/10/1614:30
 *    desc   :账号异常弹窗
 *    version: 1.0
 */
class AccountDangerDialog(val context1: Context) : Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_account_danger)
        initWindow()
    }


    companion object {
        const val VERIFY_NOTE = 0 //去认证提示
        const val VERIFY_ING = 1//认证中
        const val VERIFY_NOT_PASS = 2//未通过
        const val VERIFY_PASS = 3//通过
    }


    fun changeVerifyStatus(status: Int) {
        when (status) {
            VERIFY_NOTE -> {
                accountDangerLogo.isVisible = true
                accountDangerVerifyStatuLogo.isVisible = false
                accountDangerTitle.text = "账号异常"
                accountDangerContent.text = "您的账号存在高危风险异常\n请完成认证解除异常"
                accountDangerBtn.text = "去认证"
                accountDangerLoading.isVisible = false
                accountDangerBtn.isEnabled = true
                accountDangerBtn.onClick {
                    context1.startActivity<IDVerifyActivity>()
                }
            }
            VERIFY_ING -> {
                accountDangerLogo.isVisible = false
                accountDangerVerifyStatuLogo.isVisible = true
                accountDangerVerifyStatuLogo.setImageResource(R.drawable.icon_verify_ing)
                accountDangerTitle.text = "认证审核中"
                accountDangerContent.text = "认证审核通过后将为您解冻账号\n请耐心等待"
                accountDangerBtn.text = ""
                accountDangerLoading.isVisible = true
                accountDangerBtn.isEnabled = false

            }
            VERIFY_NOT_PASS -> {
                accountDangerLogo.isVisible = false
                accountDangerVerifyStatuLogo.isVisible = true
                accountDangerVerifyStatuLogo.setImageResource(R.drawable.icon_verify_not_pass)
                accountDangerTitle.text = "认证审核不通过"
                accountDangerContent.text = "您当前头像无法通过人脸对比\n请更换本人头像重新进行认证审核"
                accountDangerBtn.text = "修改头像"
                accountDangerLoading.isVisible = false
                accountDangerBtn.isEnabled = true
                accountDangerBtn.onClick {
                    //todo 去修改头像
                }
            }
            VERIFY_PASS -> {
                accountDangerLogo.isVisible = false
                accountDangerVerifyStatuLogo.isVisible = true
                accountDangerVerifyStatuLogo.setImageResource(R.drawable.icon_verify_pass)
                accountDangerTitle.text = "认证审核成功"
                accountDangerContent.text = "您已通过认证审核\n已为您解冻账号，现在可按正常流程操作"
                accountDangerBtn.text = "知道了"
                accountDangerLoading.isVisible = false
                accountDangerBtn.isEnabled = true
                accountDangerBtn.onClick {
                    //todo 更新此时的认证状态
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
        setCanceledOnTouchOutside(false)
    }
}