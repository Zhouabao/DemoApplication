package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseSubscriber
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.CloseDialogEvent
import com.sdy.jitangapplication.event.UpdateSendGiftEvent
import com.sdy.jitangapplication.model.GiftBean
import com.sdy.jitangapplication.nim.attachment.SendGiftAttachment
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.customer_alert_dialog_layout.cancel
import kotlinx.android.synthetic.main.customer_alert_dialog_layout.confirm
import kotlinx.android.synthetic.main.dialog_alert_candy_enough_layout.*
import org.greenrobot.eventbus.EventBus

/**
 *    author : ZFM
 *    date   : 2019/8/1513:59
 *    desc   :确定赠送糖果
 *    version: 1.0
 */
class ConfirmSendGiftDialog(var context1: Context, val giftName: GiftBean, val account: String) :
    Dialog(context1, R.style.MyDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initWindow()
        setContentView(R.layout.dialog_alert_candy_enough_layout)
        initview()
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.CENTER)
        val params = window?.attributes
        // 设置窗口背景透明度
//        params?.alpha = 0.5f
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.MATCH_PARENT
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(false)
    }

    fun initview() {
        t2.text = "你确定赠送「${giftName.title}」吗？"
        confirm.text = "赠送礼物"
        cancel.onClick {
            dismiss()
        }

        confirm.onClick {
            sendGift()
        }

    }


    fun sendGift() {
        val params = hashMapOf<String, Any>()
        params["target_accid"] = account
        params["gift_id"] = giftName.id
        RetrofitFactory.instance.create(Api::class.java)
            .giveGift(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    CommonFunction.toast(t.msg)
                    if (t.code == 200) {
                        sendGiftMessage()
                    } else if (t.code == 419) {
                        AlertCandyEnoughDialog(context).show()
                    }
                }
            })

    }

    private fun sendGiftMessage() {
        val shareSquareAttachment = SendGiftAttachment(giftName.title, giftName.icon, giftName.id)
        val message = MessageBuilder.createCustomMessage(
            account,
            SessionTypeEnum.P2P,
            "",
            shareSquareAttachment,
            CustomMessageConfig()
        )
        NIMClient.getService(MsgService::class.java).sendMessage(message, false)
            .setCallback(object :
                RequestCallback<Void?> {
                override fun onSuccess(param: Void?) {
                    //更新消息列表
                    EventBus.getDefault().post(UpdateSendGiftEvent(message))
                    //关闭自己的弹窗
                    dismiss()
                    //关闭礼物弹窗
                    EventBus.getDefault().post(CloseDialogEvent())

                }

                override fun onFailed(code: Int) {
                    dismiss()
                }

                override fun onException(exception: Throwable) {

                }
            })
    }

}