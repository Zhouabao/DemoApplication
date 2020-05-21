package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import com.blankj.utilcode.util.ActivityUtils
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
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.MatchByWishHelpEvent
import com.sdy.jitangapplication.model.UnlockBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.nim.attachment.ContactAttachment
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CommonAlertDialog
import kotlinx.android.synthetic.main.dialog_unlock_contact.*
import org.greenrobot.eventbus.EventBus

/**
 *    author : ZFM
 *    date   : 2020/5/1910:12
 *    desc   :解锁对方联系方式
 *    version: 1.0
 */
class UnlockContactDialog(
    val myContext: Context,
    val target_accid: String,
    var costCandy: Int = 0,
    var gender: Int = 0
) :
    Dialog(myContext, R.style.MyDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_unlock_contact)
        initWindow()
        initView()
    }

    private fun initView() {
        unlockBtn.text = "${costCandy}糖果解锁"


        unlockBtn.clickWithTrigger {
            dismiss()
            CommonAlertDialog.Builder(myContext)
                .setTitle("解锁提示")
                .setContent(
                    "确认以${costCandy}糖果购买${if (gender == 1) {
                        "他"
                    } else {
                        "她"
                    }}的联系方式"
                )
                .setConfirmText("确认购买")
                .setOnConfirmListener(object : CommonAlertDialog.OnConfirmListener {
                    override fun onClick(dialog: Dialog) {
                        dialog.dismiss()
                        unlockContact()
                    }
                })
                .setCancelText("取消")
                .setOnCancelListener(object : CommonAlertDialog.OnCancelListener {
                    override fun onClick(dialog: Dialog) {
                        dialog.dismiss()
                    }

                })
                .create()
                .show()
        }

    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
        val params = window?.attributes
        // 设置窗口背景透明度
//        params?.alpha = 0.5f
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
//        params?.y = SizeUtils.dp2px(10F)
        params?.windowAnimations = R.style.MyDialogBottomAnimation
        window?.attributes = params
        //点击外部可取消
        setCanceledOnTouchOutside(true)
        setCancelable(true)
    }


    /**
     * 解锁联系方式
     */
    fun unlockContact() {
        RetrofitFactory.instance.create(Api::class.java)
            .unlockContact(UserManager.getSignParams(hashMapOf("target_accid" to target_accid)))
            .excute(object : BaseSubscriber<BaseResp<UnlockBean?>>() {
                override fun onNext(t: BaseResp<UnlockBean?>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        if (t.data!!.isnew_friend) {
                            sendMatchFriendMessage()
                        } else {
                            EventBus.getDefault().post(MatchByWishHelpEvent(true, target_accid))
                            if (ActivityUtils.getTopActivity() !is ChatActivity)
                                ChatActivity.start(myContext, target_accid)
                            dismiss()
//                            sendContactMessage(t.data!!.contact_content, t.data!!.contact_way)
                        }
                    } else if (t.code == 222) { //已经解锁过
                        if (ActivityUtils.getTopActivity() !is ChatActivity)
                            ChatActivity.start(myContext, target_accid)
                        dismiss()
                    } else if (t.code == 419) {
                        RechargeCandyDialog(context).show()
                    }

                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                }
            })
    }

    fun sendContactMessage(contactContent: String, contactWay: Int) {
        val contactAttachment =
            ContactAttachment(contactContent, contactWay)
        val config = CustomMessageConfig()
        config.enablePush = false
        val message = MessageBuilder.createCustomMessage(
            target_accid,
            SessionTypeEnum.P2P,
            "",
            contactAttachment,
            config
        )

        NIMClient.getService(MsgService::class.java).sendMessage(message, false)
            .setCallback(object :
                RequestCallback<Void?> {
                override fun onSuccess(param: Void?) {
                    if (ActivityUtils.getTopActivity() is MatchDetailActivity)
                        EventBus.getDefault().post(MatchByWishHelpEvent(true, target_accid))
                    ChatActivity.start(myContext, target_accid)
                    dismiss()
                }

                override fun onFailed(code: Int) {
                }

                override fun onException(exception: Throwable) {
                }
            })
    }

    private fun sendMatchFriendMessage() {
        val wishHelpFirendAttachment = ChatHiAttachment(ChatHiAttachment.CHATHI_MATCH)
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
                    EventBus.getDefault().post(MatchByWishHelpEvent(true, target_accid))
                    if (ActivityUtils.getTopActivity() !is ChatActivity)
                        unlockBtn.postDelayed({
                            ChatActivity.start(myContext, target_accid)
                        }, 750L)
                    dismiss()
//                    sendContactMessage(contactContent, contactWay)
                }

                override fun onFailed(code: Int) {
                }

                override fun onException(exception: Throwable) {

                }
            })
    }


}