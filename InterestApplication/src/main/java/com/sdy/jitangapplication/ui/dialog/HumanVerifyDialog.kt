package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ActivityUtils
import com.kotlin.base.ext.onClick
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.ui.activity.IDVerifyActivity
import com.sdy.jitangapplication.ui.activity.NewUserInfoSettingsActivity
import kotlinx.android.synthetic.main.dialog_human_verify.*
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2019/10/1614:30
 *    desc   :聊天界面人脸认证弹窗
 *    version: 1.0
 */
class HumanVerifyDialog(val context1: Context) :
    Dialog(context1, R.style.MyDialog) {
    companion object {
        const val HUMAN_VERIFY = 1
    }

    var type: Int = HUMAN_VERIFY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_human_verify)
        initWindow()
        initView()
    }


    fun initView() {
        verifyBtn.onClick {
            if (type == HUMAN_VERIFY)
                context1.startActivity<IDVerifyActivity>("face_source_type" to 1)
            else
                if (ActivityUtils.getTopActivity() !is NewUserInfoSettingsActivity)
                    context.startActivity<NewUserInfoSettingsActivity>()

            dismiss()
        }

        closeBtn.onClick {
            dismiss()
        }

        when (type) {
            HUMAN_VERIFY -> {
                accountDangerIv.isVisible = true
                accountDangerLogo.setImageResource(R.drawable.icon_bg_verify)
                accountDangerIv.setImageResource(R.drawable.icon_verify_human)
                t1.text = "账号未认证"
                t2.text = "为保证双方社交体验\n聊天功能仅对已通过认证的用户开启"
                verifyBtn.text = "立即认证"
            }
            GotoVerifyDialog.TYPE_CHANGE_ABLUM -> {
                accountDangerIv.isVisible = false
                accountDangerLogo.setImageResource(R.drawable.icon_complete_album)
                t1.text = "完善相册"
                t2.text = "完善相册会使你的信息更多在匹配页展示\n现在就去完善你的相册吧！"
                verifyBtn.text = "立即完善"
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
        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }

}