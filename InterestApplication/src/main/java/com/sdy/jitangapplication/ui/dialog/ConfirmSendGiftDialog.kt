package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.WindowManager
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.NetworkUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.ext.onClick
import com.kotlin.base.rx.BaseException
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
import com.sdy.jitangapplication.model.SendGiftBean
import com.sdy.jitangapplication.model.SendGiftOrderBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.attachment.AccostGiftAttachment
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
class ConfirmSendGiftDialog(
    var context1: Context,
    val giftName: GiftBean,
    val account: String,
    var fromWantFriend: Boolean = false //是否来自于达成好友赠送礼物
) :
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
        t2.text = context1.getString(R.string.sure_send_left, giftName.title)
        confirm.text = context1.getString(R.string.send_gift)
        cancel.onClick {
            dismiss()
        }

        confirm.onClick {
            if (fromWantFriend) {
                sendGiftBeFriends()
            } else {
                sendGift()
            }
            dismiss()
        }

    }


    fun sendGift() {
        val params = hashMapOf<String, Any>()
        params["target_accid"] = account
        params["gift_id"] = giftName.id
        RetrofitFactory.instance.create(Api::class.java)
            .giveGift(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<SendGiftOrderBean?>>(null) {
                override fun onNext(t: BaseResp<SendGiftOrderBean?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        sendGiftMessage(t.data?.order_id ?: 0)
                        CommonFunction.toast(t.msg)
                    } else if (t.code == 419) {
                        AlertCandyEnoughDialog(
                            context1,
                            AlertCandyEnoughDialog.FROM_SEND_GIFT
                        ).show()
                    } else {
                        CommonFunction.toast(t.msg)
                    }
                }
            })

    }


    /**
     * 赠送礼物
     * code  201  次数使用完毕，请充值次数
     * code  419 你就弹框（该用户当日免费接收次数完毕，请充值会员获取）
     */
    fun sendGiftBeFriends() {
        if (!NetworkUtils.isConnected()) {
            CommonFunction.toast(context1.getString(R.string.connect_network))
            return
        }
        val loadingDialog = LoadingDialog(context1)
        RetrofitFactory.instance.create(Api::class.java)
            .sendCandy(
                UserManager.getSignParams(
                    hashMapOf(
                        "target_accid" to account,
                        "gift_id" to giftName.id
                    )
                )
            )
            .excute(object : BaseSubscriber<BaseResp<SendGiftBean?>>() {
                override fun onStart() {
                    super.onStart()
                    loadingDialog.show()
                }

                override fun onCompleted() {
                    super.onCompleted()
                    loadingDialog.dismiss()
                }

                override fun onNext(t: BaseResp<SendGiftBean?>) {
                    super.onNext(t)
                    when (t.code) {
                        200 -> {
                            CommonFunction.toast(t.msg)
                            sendAccostGiftMessage(t.data?.order_id ?: 0)
                        }
                        419 -> {//糖果余额不足
                            AlertCandyEnoughDialog(
                                context1,
                                AlertCandyEnoughDialog.FROM_SEND_GIFT
                            ).show()
                        }
                        201 -> {//需要充值会员
                            CommonFunction.startToFootPrice(context1)
                        }
                        400 -> {//错误信息
                            CommonFunction.toast(t.msg)
                        }
                    }
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    loadingDialog.dismiss()
                    if (e is BaseException) {
                        TickDialog.getInstance(context1).show()
                    }
                }
            })
    }


    private fun sendGiftMessage(orderId: Int) {
        val config = CustomMessageConfig()
        config.enableUnreadCount = true
        config.enablePush = false
        val shareSquareAttachment =
            SendGiftAttachment(orderId, SendGiftAttachment.GIFT_RECEIVE_STATUS_NORMAL)
        val message = MessageBuilder.createCustomMessage(
            account,
            SessionTypeEnum.P2P,
            "",
            shareSquareAttachment,
            config
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

                    //如果是从达成好友关系进入，就进入聊天界面
                    if (fromWantFriend) {
                        ChatActivity.start(context1, account)
                    }
                }

                override fun onFailed(code: Int) {
                    dismiss()
                }

                override fun onException(exception: Throwable) {

                }
            })
    }

    /**
     * 发送搭讪礼物消息
     */
    private fun sendAccostGiftMessage(orderId: Int) {
        val config = CustomMessageConfig()
        config.enableUnreadCount = true
        config.enablePush = false
        val accostGiftAttachment =
            AccostGiftAttachment(
                orderId,
                AccostGiftAttachment.GIFT_RECEIVE_STATUS_NORMAL,
                giftName.icon,
                giftName.title,
                giftName.amount
            )
        val message = MessageBuilder.createCustomMessage(
            account,
            SessionTypeEnum.P2P,
            "",
            accostGiftAttachment,
            config
        )
        NIMClient.getService(MsgService::class.java).sendMessage(message, false)
            .setCallback(object : RequestCallback<Void?> {
                override fun onSuccess(param: Void?) {
                    //更新消息列表
                    EventBus.getDefault().post(UpdateSendGiftEvent(message))
                    //关闭礼物弹窗
                    EventBus.getDefault().post(CloseDialogEvent())
                    //发送搭讪礼物tip消息
                    CommonFunction.sendAccostTip(account)
                    //关闭自己的弹窗
                    dismiss()
                    //跳转聊天界面
                    Handler().postDelayed({
                        ChatActivity.start(ActivityUtils.getTopActivity(), account)
                    }, 500L)
                }

                override fun onFailed(code: Int) {
                    dismiss()
                }

                override fun onException(exception: Throwable) {

                }
            })
    }

}