package com.sdy.jitangapplication.ui.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
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
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.GiftBean
import com.sdy.jitangapplication.model.SendGiftOrderBean
import com.sdy.jitangapplication.model.SendTipBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.nim.attachment.SendCustomTipAttachment
import com.sdy.jitangapplication.nim.attachment.WishHelpAttachment
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_help_wish.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *    author : ZFM
 *    date   : 2020/4/29:36
 *    desc   :聊天赠送礼物弹窗
 *    version: 1.0
 */
class HelpWishDialog(
    var myCandy_amount: Int,
    val target_accid: String,
    val nickname: String,
    val giftBean: GiftBean,
    context: Context,
    val isFriend: Boolean
) :
    BottomSheetDialog(context, R.style.BottomSheetDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_help_wish)
        initWindow()
        initView()
    }

    private fun initView() {
        confirmHelp.onClick {
            dismiss()
        }


        helpWishSeekBar.max = giftBean.amount.toFloat()
        helpWishSeekBar.min = giftBean.min_amount.toFloat()
        myCandyCount.text = "${myCandy_amount}"
        GlideUtil.loadRoundImgCenterCrop(
            context, giftBean.icon,
            giftImg, SizeUtils.dp2px(10F)
        )
        GlideUtil.loadRoundImgCenterCrop(
            context, giftBean.icon,
            giftImg1, SizeUtils.dp2px(10F)
        )
        giftCandyAmount.text = "${giftBean.amount}"
        giftName.text = "${giftBean.title}"

        //最大值
        maxHelpBtn.onClick {
            helpWishSeekBar.setProgress(giftBean.amount.toFloat())
        }

        //确认助力
        confirmHelp.onClick {
            if (myCandy_amount < helpWishSeekBar.progress) {
                CommonFunction.toast("糖果余额不足")
            } else {
                val params = hashMapOf<String, Any>(
                    "target_accid" to target_accid,
                    "amount" to helpWishSeekBar.progress,
                    "goods_id" to giftBean.id
                )
                wishHelp(params)
            }
        }

        gotoChat.onClick {
            ChatActivity.start(context, target_accid)
            dismiss()
        }

        chargeBtn.onClick {
            RechargeCandyDialog(context).show()

        }

    }


    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT

        params?.windowAnimations = R.style.MyDialogBottomAnimation
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
    }


    private fun wishHelp(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .wishHelp(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<SendGiftOrderBean?>>(null) {
                override fun onNext(t: BaseResp<SendGiftOrderBean?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        wishHelpSuccessLl.isVisible = true
                        wishHelpCl.isVisible = false
                        helpAmount.text = SpanUtils.with(helpAmount)
                            .append("助力额度")
                            .append("${helpWishSeekBar.progress}")
                            .setTypeface(
                                Typeface.createFromAsset(
                                    context.assets,
                                    "DIN_Alternate_Bold.ttf"
                                )
                            )
                            .setForegroundColor(Color.parseColor("#FF6318"))
                            .append("糖果\n你与${nickname}已达成好友关系，快去聊聊吧")
                            .create()

                        if (!isFriend) {
                            //如果不是好友并且停留在对方用户详情界面，就更改UI
                            EventBus.getDefault().post(MatchByWishHelpEvent(true, target_accid))
                            sendWishHelpFriendMessage(
                                t.data?.order_id ?: 0,
                                t.data?.ret_tips_arr ?: mutableListOf()
                            )
                        } else {
                            sendWishHelpMessage(
                                t.data?.order_id ?: 0,
                                t.data?.ret_tips_arr ?: mutableListOf()
                            )
                        }

                        EventBus.getDefault().post(RefreshCandyMallDetailEvent())
                        EventBus.getDefault()
                            .post(UpdateMyCandyAmountEvent(helpWishSeekBar.progress))
                        EventBus.getDefault().post(RefreshMyCandyEvent(-1))

                    } else {
                        CommonFunction.toast(t.msg)
                    }
                }

            })
    }


    private fun sendWishHelpMessage(orderId: Int, ret_tips_arr: MutableList<SendTipBean>) {
        val wishHelpAttachment =
            WishHelpAttachment(
                giftBean.icon,
                helpWishSeekBar.progress,
                orderId,
                WishHelpAttachment.WISH_HELP_STATUS_NORMAL
            )
        val config = CustomMessageConfig()
        config.enableUnreadCount = true
        config.enablePush = false
        val message = MessageBuilder.createCustomMessage(
            target_accid,
            SessionTypeEnum.P2P,
            "",
            wishHelpAttachment,
            config
        )
        NIMClient.getService(MsgService::class.java).sendMessage(message, false)
            .setCallback(object :
                RequestCallback<Void?> {
                override fun onSuccess(param: Void?) {
                    if (ret_tips_arr.isNotEmpty()) {
                        CommonFunction.sendTips(target_accid, ret_tips_arr)
                    }

                    //更新消息列表
                    EventBus.getDefault().post(UpdateSendGiftEvent(message))
                }

                override fun onFailed(code: Int) {
                }

                override fun onException(exception: Throwable) {

                }
            })
    }


    /**
     * 助力成功发送tip消息
     */
    private fun sendTips(retTipsArr: MutableList<SendTipBean>) {
        for (tip in retTipsArr) {
            val attachment = SendCustomTipAttachment(tip.content, tip.showType, tip.ifSendUserShow)
            val tip =
                MessageBuilder.createCustomMessage(target_accid, SessionTypeEnum.P2P, attachment)
            val config = CustomMessageConfig()
            config.enableUnreadCount = false
            config.enablePush = false
            tip.config = config
            NIMClient.getService(MsgService::class.java).sendMessage(tip, false)
                .setCallback(object :
                    RequestCallback<Void?> {
                    override fun onSuccess(param: Void?) {
                        //更新消息列表
                        EventBus.getDefault().post(UpdateSendGiftEvent(tip))
                    }

                    override fun onFailed(code: Int) {
                    }

                    override fun onException(exception: Throwable) {

                    }
                })
        }
    }

    private fun sendWishHelpFriendMessage(orderId: Int, ret_tips_arr: MutableList<SendTipBean>) {
        val wishHelpFirendAttachment = ChatHiAttachment(ChatHiAttachment.CHATHI_WISH_HELP)
        val config = CustomMessageConfig()
        config.enableUnreadCount = true
        config.enablePush = true
        val message = MessageBuilder.createCustomMessage(
            target_accid,
            SessionTypeEnum.P2P,
            "",
            wishHelpFirendAttachment,
            config
        )

        NIMClient.getService(MsgService::class.java).sendMessage(message, false)
            .setCallback(object :
                RequestCallback<Void?> {
                override fun onSuccess(param: Void?) {
                    //更新消息列表
                    EventBus.getDefault().post(UpdateSendGiftEvent(message))
                    sendWishHelpMessage(orderId, ret_tips_arr)
                }

                override fun onFailed(code: Int) {
                }

                override fun onException(exception: Throwable) {

                }
            })
    }


    override fun show() {
        super.show()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCloseDialogEvent(event: CloseDialogEvent) {
        if (isShowing) {
            dismiss()
        }
    }
}