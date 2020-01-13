package com.sdy.jitangapplication.ui.dialog

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
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.ui.activity.NewUserInfoSettingsActivity
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.change_avator_real_man_dialog_layout.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/10/1614:30
 *    desc   : 提醒去替换头像
 *    version: 1.0
 */
class ChangeAvatarRealManDialog(
    val context1: Context,
    var status: Int = VERIFY_NEED_REAL_MAN,
    var avator: String = ""
) :
    Dialog(context1, R.style.MyDialog) {

    companion object {
        const val VERIFY_NEED_REAL_MAN = 0 //替换真人
        const val VERIFY_NEED_VALID_REAL_MAN = 1 //替换为合规的真人照片
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_avator_real_man_dialog_layout)
        initWindow()
        initView()
    }

    private fun initView() {
        GlideUtil.loadCircleImg(context1, UserManager.getAvator(), accountImg)
        close.onClick {
            dismiss()
        }
        accountDangerBtn.onClick {
            context1.startActivity<NewUserInfoSettingsActivity>()
            dismiss()
        }
        when (status) {
            VERIFY_NEED_REAL_MAN -> {
                close.isVisible = true
                accountDangerTitle.text = "请更换真人头像"
                accountDangerContent.text = "由于未使用真人头像\n真实头像用户对您暂不可见"
                accountDangerBtn.text = "立即替换"
            }
            else -> {
                close.isVisible = false
                accountDangerTitle.text = "请替换头像"
                accountDangerContent.text = "当前头像不符合标准\n请替换头像"
                accountDangerBtn.text = "立即替换"
            }
        }


    }

    override fun dismiss() {
        super.dismiss()
        UserManager.saveAlertChangeRealMan(true)
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
        setCanceledOnTouchOutside(true)
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