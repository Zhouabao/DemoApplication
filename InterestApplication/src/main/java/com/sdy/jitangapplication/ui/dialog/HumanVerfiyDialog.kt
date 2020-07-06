package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.isVisible
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
import com.sdy.jitangapplication.ui.activity.NewUserInfoSettingsActivity
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_account_danger.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/10/1614:30
 *    desc   :发起人工审核认证
 *    version: 1.0
 */
class HumanVerfiyDialog(val context1: Context, val type: Int, val showToast: Boolean) :
    Dialog(context1, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_account_danger)
        initWindow()
        changeVerifyStatus()
    }

    fun changeVerifyStatus() {
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
        accountDangerTitle.text = "认证审核不通过"
        accountDangerContent.text = "您当前头像无法通过人脸对比\n请更换本人头像重新进行认证审核"
        accountDangerBtn.text = "修改头像"
        humanVerify.setTextColor(Color.parseColor("#FFFF6318"))
        accountDangerLoading.isVisible = false
        accountDangerBtn.isEnabled = true
        accountDangerBtn.onClick {
            context1.startActivity<NewUserInfoSettingsActivity>(
                "showToast" to true,
                "type" to type
            )
            dismiss()
        }
        humanVerify.onClick {
            humanVerify(1)
        }

        closeBtn.clickWithTrigger {
            dismiss()
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
        setCancelable(true)
        setCanceledOnTouchOutside(false)
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
                        //todo 新增审核中状态
                        CommonFunction.toast("已提交人工审核，请耐心等待")
                        dismiss()
                    }
                }

                override fun onError(e: Throwable?) {

                }
            })

    }

}