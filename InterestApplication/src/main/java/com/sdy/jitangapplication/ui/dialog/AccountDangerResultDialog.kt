package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
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
import com.sdy.jitangapplication.ui.activity.NewUserInfoSettingsActivity
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_account_danger_result.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/10/1614:30
 *    desc   :账号异常弹窗
 *    version: 1.0
 */
class AccountDangerResultDialog(
    val context1: Context,
    var status: Int = AccountDangerDialog.VERIFY_NOT_PASS
) :
    Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_account_danger_result)
        initWindow()
        changeVerifyStatus(status)
    }


    fun changeVerifyStatus(status: Int) {

        GlideUtil.loadCircleImg(context1, UserManager.getAvator(), accountDangerVerifyStatuLogo1)
        when (status) {
            AccountDangerDialog.VERIFY_ING -> {
                accountDangerImgAlert1.isVisible = false
                humanVerify.isVisible = false
                accountDangerTitle1.text = context1.getString(R.string.verify_account_ing)
                accountDangerContent1.text = context1.getString(R.string.please_wait_to_clear_danger)
                accountDangerBtn1.text = context1.getString(R.string.waiting)
                accountDangerBtn1.isEnabled = false
            }
            AccountDangerDialog.VERIFY_NOT_PASS -> {
                accountDangerImgAlert1.isVisible = true
                humanVerify.isVisible = true
//                accountDangerVerifyStatuLogo1.setImageResource(R.drawable.icon_verify_account_not_pass)
                accountDangerTitle1.text = context1.getString(R.string.avata_verify_fail)
                accountDangerContent1.text = context1.getString(R.string.avatar_cannot_pass_verify)
                accountDangerBtn1.text = context1.getString(R.string.change_avatar)
                humanVerify.setTextColor(Color.parseColor("#FFFF6318"))
                accountDangerBtn1.isEnabled = true
                accountDangerBtn1.onClick {
                    if (ActivityUtils.getTopActivity() !is NewUserInfoSettingsActivity)
                        context1.startActivity<NewUserInfoSettingsActivity>()
                    else
                        dismiss()
                }
                humanVerify.onClick {
                    humanVerify(1)
                }

            }
            AccountDangerDialog.VERIFY_NOT_PASS_FORCE -> {
                accountDangerImgAlert1.isVisible = false
                humanVerify.isVisible = true
                accountDangerTitle1.text = context1.getString(R.string.avata_verify_fail)
                accountDangerContent1.text = context1.getString(R.string.avatar_cannot_pass_verify)
                accountDangerBtn1.text = context1.getString(R.string.change_avatar)
                accountDangerBtn1.isEnabled = true
                accountDangerBtn1.onClick {
                    if (ActivityUtils.getTopActivity() !is NewUserInfoSettingsActivity)
                        context1.startActivity<NewUserInfoSettingsActivity>()
                    else
                        dismiss()
                }
                humanVerify.onClick {
                    humanVerify(1)
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
                    if (t.code == 200) {
                        changeVerifyStatus(AccountDangerDialog.VERIFY_ING)
                        CommonFunction.toast(context1.getString(R.string.has_commit_human_verify))
                    }
                }

                override fun onError(e: Throwable?) {

                }
            })

    }

}