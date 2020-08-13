package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.SpanUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
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
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.ChatUpBean
import com.sdy.jitangapplication.model.UnlockBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.attachment.ContactCandyAttachment
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_chat_up_open_pt_vip.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *    author : ZFM
 *    date   : 2020/5/99:45
 *    desc   :解锁聊天界面
 *    1.高级会员：
 *      1.1有无微信
 *      1.2有无次数
 *      1.3有无上线

 *
 *    2.非高级会员：
 *      2.1.有无微信
 *      2.2.有无上线
 *
 */
class ChatUpOpenPtVipDialog(
    val context1: Context,
    val target_accid: String,
    val type: Int = TYPE_CHAT,
    val chatUpBean: ChatUpBean,
    val msg: String = ""
) :
    Dialog(context1, R.style.MyDialog) {

    companion object {
        const val TYPE_CHAT = 1
        const val TYPE_CONTACT = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_chat_up_open_pt_vip)
        initWindow()
        initChatData()
        EventBus.getDefault().register(this)
    }


    private fun initChatData() {
        GlideUtil.loadCircleImg(context1, chatUpBean.avatar, chatupAvator)

        //	0没有留下联系方式 1 电话 2 微信 3 qq 99隐藏
        when (chatUpBean.contact_way) {
            1 -> {
                chatupContact.setBackgroundResource(R.drawable.shape_rectangle_light_orange_16dp)
                chatupContact.setCompoundDrawablesWithIntrinsicBounds(
                    context1.resources.getDrawable(R.drawable.icon_phone_reg),
                    null,
                    null,
                    null
                )
                SpanUtils.with(chatupContact)
                    .append("手机")
                    .setForegroundColor(Color.parseColor("#FFFF6318"))
                    .append("\t${chatUpBean.contact}")
                    .setForegroundColor(Color.parseColor("#FFFF6318"))
                    .setBold()
                    .create()
            }
            2 -> {
                chatupContact.setBackgroundResource(R.drawable.shape_rectangle_green_16dp)
                chatupContact.setCompoundDrawablesWithIntrinsicBounds(
                    context1.resources.getDrawable(R.drawable.icon_wechat_reg),
                    null,
                    null,
                    null
                )
                SpanUtils.with(chatupContact)
                    .append("微信")
                    .setForegroundColor(Color.parseColor("#FF1EC121"))
                    .append("\t${chatUpBean.contact}")
                    .setForegroundColor(Color.parseColor("#FF1EC121"))
                    .setBold()
                    .create()
            }
            3 -> {
                chatupContact.setBackgroundResource(R.drawable.shape_rectangle_blue_solid_16dp)
                chatupContact.setCompoundDrawablesWithIntrinsicBounds(
                    context1.resources.getDrawable(R.drawable.icon_qq_reg),
                    null,
                    null,
                    null
                )
                SpanUtils.with(chatupContact)
                    .append("QQ")
                    .setForegroundColor(Color.parseColor("#FF1E9CF0"))
                    .append("\t${chatUpBean.contact}")
                    .setForegroundColor(Color.parseColor("#FF1E9CF0"))
                    .setBold()
                    .create()
            }
        }

        when (type) {
            /**
             * 1.非高级会员
             *      1.1设置私聊权限
             *              a.她仅允许高级用户联系她
             *              b.立即成为高级用户，不要错过
             *              c.成为高级会员，免费无限次聊天
             *      1.2未设置私聊权限
             *          1.2.1次数未用尽
             *              a.获得聊天机会
             *              b.今日还有3次免费聊天机会
             *              (解锁聊天)
             *              c.成为高级会员，免费无限次聊天
             *
             *          1.2.2次数用尽
             *              a.获得聊天机会
             *              b.今日聊天机会已用完
             *              (解锁聊天  30糖果)
             *              c.成为高级会员，免费无限次聊天
             *
             * 2.高级会员
             *      2.1聊天次数未用尽
             *          a.要给她打个招呼吗
             *          b.今日还可以免费10次聊天
             *          （微信 wei****5）
             *          c.解锁聊天
             *      2.2聊天次数用尽
             *          a.今日免费次数已用完
             *          b.今日免费聊天次数已用完
             *          （微信 wei****5）
             *          c.解锁聊天（30糖果）
             *
             */

            TYPE_CHAT -> { //解锁聊天
                openPtVipBtn.setBackgroundResource(R.drawable.gradient_gold_vip)
                chatupContact.isVisible = false
                if (chatUpBean.isplatinum) {
                    chatupUnlockChat.isVisible = false
                    chatupTitle.text = "获得聊天机会"
                    openPtVipBtn.text = "成为黄金会员，免费更多聊天"
                    if (chatUpBean.plat_cnt > 0) {
                        chatupTitle.text = "要给她打个招呼吗"
                        chatupContent.text = "今日还可免费解锁${chatUpBean.plat_cnt}次聊天"
                        openPtVipBtn.text = "解锁聊天"

                    } else {
                        chatupTitle.text = "获得聊天机会"
                        chatupContent.text = "聊天需要消耗糖果，向对方表达诚意"
                        openPtVipBtn.text = "解锁聊天 （${chatUpBean.chat_amount}糖果）"
                    }
                    // 解锁聊天
                    openPtVipBtn.clickWithTrigger {
                        unlockChat()
                    }
                } else {
                    //成为高级会员
                    openPtVipBtn.clickWithTrigger {
                        CommonFunction.startToVip(context1)
                        dismiss()
                    }

                    if (chatUpBean.private_chat_btn) {
                        chatupTitle.text = "她设置了等级权限"
                        chatupContent.text = "她仅允许高级用户联系她\n立即成为高级用户，不要错过她"
                        chatupUnlockChat.isVisible = false
                        openPtVipBtn.text = "成为黄金会员，证明实力解锁关系"
                    } else {
                        openPtVipBtn.text = "成为黄金会员，免费更多聊天"
                        chatupTitle.text = "获得聊天机会"
                        chatupUnlockChat.isVisible = true
                        if (chatUpBean.plat_cnt > 0) {
                            chatupContent.text = "今日还有${chatUpBean.plat_cnt}次聊天机会"
                            chatupUnlockChat.text = "解锁聊天"
                        } else {
                            chatupContent.text = "今日免费聊天机会已用完"
                            chatupUnlockChat.text = "解锁聊天 （${chatUpBean.chat_amount}糖果）"
                        }
                        // 解锁聊天
                        chatupUnlockChat.clickWithTrigger {
                            unlockChat()
                        }
                    }
                }
            }
            TYPE_CONTACT -> { //解锁联系方式
                /**
                 * 1.是否是直联卡
                 *  1.1 聊天次数未用尽
                 *      a.微信 Wei****5
                 *      b.是否解锁她的联系方式
                 *      c.您当日还可免费解锁10次联系方式
                 *      d.解锁她的联系方式
                 *
                 *  1.2 聊天次数用尽
                 *      a.微信 Wei****5
                 *      b.今日免费解锁次数已用完
                 *      c.您当日还可以免费解锁0次联系方式\n使用糖果解锁，不错过心仪的她
                 *      d.解锁她的联系方式（200糖果）
                 * 2.非高级会员
                 *      a.微信 Wei****5
                 *      b.解锁心仪的她
                 *      c.解锁联系方式（200糖果）
                 *      d.购买至尊直联卡，免费解锁联系方式
                 *
                 */
                chatupContact.isVisible = true
                if (chatUpBean.private_chat_btn && !chatUpBean.isplatinum) {
                    openPtVipBtn.setBackgroundResource(R.drawable.gradient_gold_vip)
                    //todo 对方设置了聊天权限 则成为钻石会员才能开启聊天
                    chatupTitle.text = "她设置了等级权限"
                    chatupContent.text = "她仅允许高级用户联系她\n立即成为高级用户，不要错过她"
                    chatupUnlockChat.isVisible = false
                    openPtVipBtn.text = "成为黄金会员，证明实力解锁关系"
                    openPtVipBtn.clickWithTrigger {
                        CommonFunction.startToVip(context1)
                        dismiss()
                    }

                } else {
                    openPtVipBtn.setBackgroundResource(R.drawable.gradient_contact_card)
                    if (chatUpBean.isdirect) {//是直联卡会员,判断有没有次数
                        chatupUnlockChat.isVisible = false
                        chatupContent.isVisible = true
                        if (chatUpBean.direct_residue_cnt > 0) {
                            chatupTitle.text = "是否解锁她的联系方式"
                            chatupContent.text = "您当日还可免费解锁${chatUpBean.direct_residue_cnt}次联系方式"
                            openPtVipBtn.text = "解锁她的联系方式"
                        } else {
                            chatupTitle.text = "免费解锁次数已用完"
                            chatupContent.text = "您当日还可以免费解锁0次联系方式\n使用糖果解锁，不错过心仪的她"
                            openPtVipBtn.text = "解锁联系方式 （${chatUpBean.contact_amount}糖果）"
                        }

                        // 解锁联系方式
                        openPtVipBtn.clickWithTrigger {
                            unlockContact()
                        }
                    } else {  //不是的话,弹起购买直联卡
                        chatupUnlockChat.isVisible = true
                        chatupContent.isVisible = false
                        chatupTitle.text = "解锁心仪的她"
                        chatupUnlockChat.text = "解锁联系方式 （${chatUpBean.contact_amount}糖果）"
                        // 解锁联系方式
                        chatupUnlockChat.clickWithTrigger {
                            unlockContact()
                        }
                        openPtVipBtn.text = "成为钻石会员，免费解锁联系方式"
                        // 购买直联卡
                        openPtVipBtn.clickWithTrigger {
                            CommonFunction.startToVip(context1, 1)
                        }
                    }
                }
            }

        }
    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
//        window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        params?.windowAnimations = R.style.MyDialogBottomAnimation
        window?.attributes = params
        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }


    /**
     * 发送本地搭讪好友消息
     */
    private fun sendMatchFriendMessage(loadingDialog: LoadingDialog) {
        EventBus.getDefault().post(UpdateApproveEvent())
        EventBus.getDefault().post(UpdateHiEvent())
        EventBus.getDefault().post(UpdateAccostListEvent())
        if (ActivityUtils.getTopActivity() is MatchDetailActivity)
            EventBus.getDefault().post(MatchByWishHelpEvent(true, target_accid))
        if (ActivityUtils.getTopActivity() !is ChatActivity) {
            Handler().postDelayed({
                loadingDialog.dismiss()
                ChatActivity.start(ActivityUtils.getTopActivity(), target_accid)
            }, 400L)
        } else {
            loadingDialog.dismiss()
        }
        dismiss()


//        val wishHelpFirendAttachment = ChatHiAttachment(ChatHiAttachment.CHATHI_CHATUP_FRIEND)
//        val config = CustomMessageConfig()
//        config.enablePush = false
//        val message = MessageBuilder.createCustomMessage(
//            target_accid,
//            SessionTypeEnum.P2P,
//            "",
//            wishHelpFirendAttachment,
//            config
//        )
//
//        NIMClient.getService(MsgService::class.java).sendMessage(message, false)
//            .setCallback(object : RequestCallback<Void?> {
//                override fun onSuccess(param: Void?) {
//                    EventBus.getDefault().post(UpdateApproveEvent())
//                    EventBus.getDefault().post(UpdateHiEvent())
//                    EventBus.getDefault().post(UpdateAccostListEvent())
//                    if (ActivityUtils.getTopActivity() is MatchDetailActivity)
//                        EventBus.getDefault().post(MatchByWishHelpEvent(true, target_accid))
//                    if (ActivityUtils.getTopActivity() !is ChatActivity) {
//                        Handler().postDelayed({
//                            loadingDialog.dismiss()
//                            ChatActivity.start(ActivityUtils.getTopActivity(), target_accid)
//                        }, 400L)
//                    } else {
//                        EventBus.getDefault().post(UpdateSendGiftEvent(message))
//                        loadingDialog.dismiss()
//                    }
//                    dismiss()
////                    sendContactMessage(contactContent, contactWay)
//                }
//
//                override fun onFailed(code: Int) {
//                    loadingDialog.dismiss()
//                    EventBus.getDefault().post(UpdateApproveEvent())
//                    EventBus.getDefault().post(UpdateHiEvent())
//                    EventBus.getDefault().post(UpdateAccostListEvent())
//                }
//
//                override fun onException(exception: Throwable) {
//                    loadingDialog.dismiss()
//                    EventBus.getDefault().post(UpdateApproveEvent())
//                    EventBus.getDefault().post(UpdateHiEvent())
//                    EventBus.getDefault().post(UpdateAccostListEvent())
//                }
//            })
    }


    /**
     * 发送解锁聊天消息
     */
    private fun sendContactCandyMessage(loadingDialog: LoadingDialog) {
        val contactCandyMsg = ContactCandyAttachment(chatUpBean.contact_amount)
        val config = CustomMessageConfig()
        config.enablePush = true
        val message = MessageBuilder.createCustomMessage(
            target_accid,
            SessionTypeEnum.P2P,
            "",
            contactCandyMsg,
            config
        )

        NIMClient.getService(MsgService::class.java).sendMessage(message, false)
            .setCallback(object : RequestCallback<Void?> {
                override fun onSuccess(param: Void?) {
                    if (ActivityUtils.getTopActivity() is MatchDetailActivity)
                        EventBus.getDefault().post(MatchByWishHelpEvent(true, target_accid))
                    if (ActivityUtils.getTopActivity() !is ChatActivity) {
                        Handler().postDelayed({
                            loadingDialog.dismiss()
                            ChatActivity.start(ActivityUtils.getTopActivity(), target_accid)
                            dismiss()
                        }, 250L)
                    } else {
                        loadingDialog.dismiss()
                    }
                }

                override fun onFailed(code: Int) {
                    loadingDialog.dismiss()
                }

                override fun onException(exception: Throwable) {
                    loadingDialog.dismiss()
                }
            })
    }


    override fun show() {
        super.show()
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


    /**
     * 解锁联系方式
     * 200 解锁成功 419 糖果余额不足
     */
    fun unlockContact() {
        val loading = LoadingDialog(context1)
        RetrofitFactory.instance.create(Api::class.java)
            .unlockContact(UserManager.getSignParams(hashMapOf("target_accid" to target_accid)))
            .excute(object : BaseSubscriber<BaseResp<UnlockBean?>>() {
                override fun onStart() {
                    super.onStart()
                    loading.show()
                }

                override fun onNext(t: BaseResp<UnlockBean?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        if (ActivityUtils.getTopActivity() is MatchDetailActivity)
                            EventBus.getDefault().post(MatchByWishHelpEvent(true, target_accid))
                        if (ActivityUtils.getTopActivity() !is ChatActivity) {
                            Handler().postDelayed({
                                loading.dismiss()
                                ChatActivity.start(ActivityUtils.getTopActivity(), target_accid)
                                dismiss()
                            }, 250L)
                        } else {
                            EventBus.getDefault().post(HideContactLlEvent())
                            loading.dismiss()
                        }
                    } else if (t.code == 419) {
                        loading.dismiss()
                        CommonFunction.gotoCandyRecharge(context1)
                    } else {
                        loading.dismiss()
                        CommonFunction.toast(t.msg)
                    }
                    dismiss()
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    loading.dismiss()
                }
            })
    }


    /**
     * 解锁聊天
     * 	201 门槛
     * 	206 好友跳聊天
     * 	419 糖果余额不足
     * 	200 解锁成功
     */
    fun unlockChat() {
        val loading = LoadingDialog(context1)
        RetrofitFactory.instance.create(Api::class.java)
            .unlockChat(UserManager.getSignParams(hashMapOf("target_accid" to target_accid)))
            .excute(object : BaseSubscriber<BaseResp<UnlockBean?>>() {
                override fun onStart() {
                    super.onStart()
                    loading.show()
                }

                override fun onNext(t: BaseResp<UnlockBean?>) {
                    super.onNext(t)
                    if (t.code == 201) {
                        loading.dismiss()
                        CommonFunction.startToFootPrice(context1)
                    } else if (t.code == 200) {
                        sendMatchFriendMessage(loading)
                    } else if (t.code == 206) {
                        loading.dismiss()
                        ChatActivity.start(ActivityUtils.getTopActivity(), target_accid)
                    } else if (t.code == 419) {
                        loading.dismiss()
                        CommonFunction.gotoCandyRecharge(context1)
                    } else {
                        loading.dismiss()
                        CommonFunction.toast(t.msg)
                    }
                    dismiss()
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    loading.dismiss()
                }
            })
    }


    /**
     * 男性解锁糖果聊天
     */
    fun lockChatup() {
        val loadingDialog = LoadingDialog(context1)
        val params = hashMapOf<String, Any>("target_accid" to target_accid)
        RetrofitFactory.instance.create(Api::class.java)
            .lockChatup(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(null) {
                override fun onStart() {
                    super.onStart()
                    loadingDialog.show()
                }


                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    loadingDialog.dismiss()
                    if (t.code == 200) {
                        EventBus.getDefault().post(UpdateApproveEvent())
                        EventBus.getDefault().post(UpdateHiEvent())
                        dismiss()
                    } else if (t.code == 201) {
                        OpenVipActivity.start(context1)
                        dismiss()
                    } else if (t.code == 419) {
                        AlertCandyEnoughDialog(
                            context1,
                            AlertCandyEnoughDialog.FROM_SEND_GIFT
                        ).show()
                        dismiss()
                    }
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    loadingDialog.dismiss()
                }
            })
    }
}