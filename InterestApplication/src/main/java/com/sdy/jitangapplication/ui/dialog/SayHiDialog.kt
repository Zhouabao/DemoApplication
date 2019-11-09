package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.kotlin.base.ext.onClick
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import kotlinx.android.synthetic.main.dialog_say_hi.*

/**
 *    author : ZFM
 *    date   : 2019/11/99:44
 *    desc   :打招呼dialog
 *    version: 1.0
 */
class SayHiDialog(
    val target_accid: String,
    val userName: String,
    val context1: Context
) : Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_say_hi)
        initWindow()

        initView()
    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.WRAP_CONTENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogCenterAnimation
        window?.attributes = params
        setCanceledOnTouchOutside(true)
    }

    private fun initView() {
        sayHitargetName.text = userName
        sayHiClose.onClick {
            dismiss()
        }
        sayHiBtn.onClick {
            if (!sayHiContent.text.isNullOrBlank()) {
                val msg = MessageBuilder.createTextMessage(
                    target_accid,
                    SessionTypeEnum.P2P,
                    sayHiContent.text.toString().trim()
                )

                NIMClient.getService(MsgService::class.java).sendMessage(msg, false).setCallback(object :
                    RequestCallback<Void?> {
                    override fun onSuccess(p0: Void?) {
                        dismiss()
                    }

                    override fun onFailed(p0: Int) {
                    }

                    override fun onException(p0: Throwable?) {

                    }

                })
            }


            CommonFunction.toast("打招呼")
        }


    }

}