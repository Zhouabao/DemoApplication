package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.Window
import android.view.WindowManager
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.BarUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
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
        const val VERIFY_NOT_PASS_FORCE = 3//强制认证未通过
        const val VERIFY_PASS = 3//通过
    }


    fun changeVerifyStatus(status: Int) {
        when (status) {
            VERIFY_NEED_AVATOR_INVALID -> {
                accountDangerResultCl.isVisible = false
                accountDangerCl.isVisible = true

                accountDangerLogo.isVisible = false
                accountDangerImgAlert.isVisible = true
                humanVerify.isVisible = false
                accountDangerVerifyStatuLogo.isVisible = true
                GlideUtil.loadImg(context1, UserManager.getAvator(), accountDangerVerifyStatuLogo)
                accountDangerTitle.text = context1.getString(R.string.face_verify)
                accountDangerContent.text = context1.getString(R.string.please_verify_face)
                accountDangerBtn.text = context1.getString(R.string.goto_verify)
                accountDangerLoading.isVisible = false
                accountDangerBtn.isEnabled = true
                accountDangerBtn.onClick {
                    CommonFunction.startToFace(context1)
                }
            }
            VERIFY_NEED_ACCOUNT_DANGER -> {
                accountDangerResultCl.isVisible = false
                accountDangerCl.isVisible = true

                accountDangerImgAlert.isVisible = false
                humanVerify.isVisible = false
                accountDangerLogo.isVisible = true
                accountDangerVerifyStatuLogo.isVisible = false
                accountDangerTitle.text = context1.getString(R.string.account_danger)
                accountDangerContent.text =
                    context1.getString(R.string.please_verify_to_clear_danger)
                accountDangerBtn.text = context1.getString(R.string.goto_verify)
                accountDangerLoading.isVisible = false
                accountDangerBtn.isEnabled = true
                accountDangerBtn.onClick {
                    CommonFunction.startToFace(context1, IDVerifyActivity.TYPE_ACCOUNT_DANGER)
                }
            }

            VERIFY_ING -> {
                accountDangerResultCl.isVisible = true
                accountDangerCl.isVisible = false

                GlideUtil.loadCircleImg(
                    context1,
                    UserManager.getAvator(),
                    accountDangerVerifyStatuLogo1
                )
                accountDangerImgAlert1.isVisible = false
                humanVerify1.isVisible = false
                accountDangerTitle1.text = context1.getString(R.string.verify_account_ing)
                accountDangerContent1.text =
                    context1.getString(R.string.please_wait_to_clear_danger)
                accountDangerBtn1.text = context1.getString(R.string.waiting)
                accountDangerBtn1.isEnabled = false
            }
            VERIFY_NOT_PASS -> {
                accountDangerResultCl.isVisible = true
                accountDangerCl.isVisible = false

                GlideUtil.loadCircleImg(
                    context1,
                    UserManager.getAvator(),
                    accountDangerVerifyStatuLogo1
                )
                accountDangerImgAlert1.isVisible = true
                humanVerify1.isVisible = true
//                accountDangerVerifyStatuLogo1.setImageResource(R.drawable.icon_verify_account_not_pass)
                accountDangerTitle1.text = context1.getString(R.string.avata_verify_fail)
                accountDangerContent1.text = context1.getString(R.string.avatar_cannot_pass_verify)
                accountDangerBtn1.text = context1.getString(R.string.change_avatar)
                humanVerify1.setTextColor(Color.parseColor("#FFFF6318"))
                accountDangerBtn1.isEnabled = true
                accountDangerBtn1.onClick {
                    if (ActivityUtils.getTopActivity() !is NewUserInfoSettingsActivity)
                        context1.startActivity<NewUserInfoSettingsActivity>()
                    else
                        dismiss()
                }
                humanVerify1.onClick {
                    humanVerify(1)
                }

            }
            VERIFY_NOT_PASS_FORCE -> {
                accountDangerResultCl.isVisible = true
                accountDangerCl.isVisible = false

                GlideUtil.loadCircleImg(
                    context1,
                    UserManager.getAvator(),
                    accountDangerVerifyStatuLogo1
                )
                accountDangerImgAlert1.isVisible = false
                humanVerify1.isVisible = true
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
                humanVerify1.onClick {
                    humanVerify(1)
                }

//                accountDangerResultCl.isVisible = false
//                accountDangerCl.isVisible = true
//                accountDangerImgAlert.isVisible = false
//                humanVerify.isVisible = true
//                accountDangerLogo.isVisible = false
//                accountDangerVerifyStatuLogo.isVisible = true
//                accountDangerVerifyStatuLogo.setImageResource(R.drawable.icon_verify_account_not_pass)
//                accountDangerTitle.text = context1.getString(R.string.avata_verify_fail)
//                accountDangerContent.text = context1.getString(R.string.avatar_cannot_pass_verify)
//                accountDangerBtn.text = context1.getString(R.string.change_avatar)
//                accountDangerLoading.isVisible = false
//                accountDangerBtn.isEnabled = true
//                accountDangerBtn.onClick {
//                    if (ActivityUtils.getTopActivity() !is NewUserInfoSettingsActivity)
//                        context1.startActivity<NewUserInfoSettingsActivity>()
//                    else
//                        dismiss()
//                }
//                humanVerify.onClick {
//                    humanVerify(1)
//                }
            }
            VERIFY_PASS -> {
                accountDangerResultCl.isVisible = false
                accountDangerCl.isVisible = true

                accountDangerImgAlert.isVisible = false
                humanVerify.isVisible = false
                accountDangerLogo.isVisible = false
                accountDangerVerifyStatuLogo.isVisible = true
                accountDangerVerifyStatuLogo.setImageResource(R.drawable.icon_verify_account_pass)
                accountDangerTitle.text = context1.getString(R.string.verify_success)
                accountDangerContent.text = context1.getString(R.string.account_has_clear_danger)
                accountDangerBtn.text = context1.getString(R.string.iknow)
                accountDangerLoading.isVisible = false
                accountDangerBtn.isEnabled = true
                accountDangerBtn.onClick {
                    dismiss()
                }
            }

        }
    }


    fun changeVerifyResultStatus(status: Int) {
        when (status) {
            VERIFY_ING -> {
                accountDangerImgAlert.isVisible = false
                humanVerify.isVisible = false
                accountDangerLogo.isVisible = false
                accountDangerVerifyStatuLogo.isVisible = true
                accountDangerVerifyStatuLogo.setImageResource(R.drawable.icon_verify_account_ing)
                accountDangerTitle.text = context1.getString(R.string.verify_account_ing)
                accountDangerContent.text = context1.getString(R.string.please_wait_to_clear_danger)
                accountDangerBtn.text = ""
                accountDangerLoading.isVisible = true
                accountDangerBtn.isEnabled = false
            }
            VERIFY_NOT_PASS -> {
                closeBtn.isVisible = true
                accountDangerImgAlert.isVisible = true
                humanVerify.isVisible = true
                accountDangerLogo.isVisible = false
                accountDangerVerifyStatuLogo.isVisible = true
                GlideUtil.loadCircleImg(
                    context1,
                    UserManager.getAvator(),
                    accountDangerVerifyStatuLogo
                )
//                accountDangerVerifyStatuLogo.setImageResource(R.drawable.icon_verify_account_not_pass)
                accountDangerTitle.text = context1.getString(R.string.avata_verify_fail)
                accountDangerContent.text = context1.getString(R.string.avatar_cannot_pass_verify)
                accountDangerBtn.text = context1.getString(R.string.change_avatar)
                humanVerify.setTextColor(Color.parseColor("#FFFF6318"))
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

                closeBtn.clickWithTrigger {
                    dismiss()
                }
            }
            VERIFY_NOT_PASS_FORCE -> {
                accountDangerImgAlert.isVisible = false
                humanVerify.isVisible = true
                accountDangerLogo.isVisible = false
                accountDangerVerifyStatuLogo.isVisible = true
                accountDangerVerifyStatuLogo.setImageResource(R.drawable.icon_verify_account_not_pass)
                accountDangerTitle.text = context1.getString(R.string.avata_verify_fail)
                accountDangerContent.text = context1.getString(R.string.avatar_cannot_pass_verify)
                accountDangerBtn.text = context1.getString(R.string.change_avatar)
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
        }
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        if (status == VERIFY_ING || status == VERIFY_NOT_PASS_FORCE || status == VERIFY_NOT_PASS) {
            params?.width = WindowManager.LayoutParams.MATCH_PARENT
            params?.height = WindowManager.LayoutParams.MATCH_PARENT
            params?.windowAnimations = R.style.MyDialogBottomAnimation
            BarUtils.setStatusBarColor(window!!,Color.WHITE)

        } else {
            params?.width = WindowManager.LayoutParams.WRAP_CONTENT
            params?.height = WindowManager.LayoutParams.WRAP_CONTENT
            params?.windowAnimations = R.style.MyDialogCenterAnimation

        }

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
                        changeVerifyStatus(VERIFY_ING)
                        CommonFunction.toast(context1.getString(R.string.has_commit_human_verify))
                    }
                }

                override fun onError(e: Throwable?) {

                }
            })

    }

}