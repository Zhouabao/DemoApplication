package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.isVisible
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
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.ApplyDatingBean
import com.sdy.jitangapplication.model.CheckPublishDatingBean
import com.sdy.jitangapplication.model.DatingBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.attachment.ChatDatingAttachment
import com.sdy.jitangapplication.ui.activity.VipPowerActivity
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.dialog_dating_open_pt_vip.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *
 * 约会开通会员
 */
class DatingOpenPtVipDialog(
    val context1: Context,
    val type: Int = TYPE_DATING_PUBLISH,
    val chatUpBean: CheckPublishDatingBean? = null,
    val datingBean: DatingBean? = null
) :
    Dialog(context1, R.style.MyDialog) {

    companion object {
        val TYPE_DATING_PUBLISH = 1 //发布约会
        val TYPE_DATING_APPLYFOR = 2 //报名约会
        val TYPE_DATING_APPLYFOR_PRIVACY = 3 //限制会员
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_dating_open_pt_vip)
        initWindow()
        initChatData()
        EventBus.getDefault().register(this)
    }


    private fun initChatData() {
        when (type) {
            TYPE_DATING_PUBLISH -> { //发布约会
                GlideUtil.loadCircleImg(context1, UserManager.getAvator(), datingAvator)
                openPtVipBtn.setBackgroundResource(R.drawable.gradient_gold_vip)
                datingTitle.text = "仅黄金会员能发布活动"
                datingContent.text = "活动为黄金会员独享功能\n成为黄金会员，建立和她的浪漫活动吧"
                openPtVipBtn.text = "成为黄金会员"
                applyForDatingBtn.isVisible = false
                openPtVipBtn.clickWithTrigger {
                    CommonFunction.startToVip(context1, VipPowerActivity.SOURCE_FREE_DATING)
                }
            }
            TYPE_DATING_APPLYFOR -> {
                //1.先判断有无高级限制
                if (chatUpBean != null) {
                    GlideUtil.loadCircleImg(context1, datingBean?.avatar, datingAvator)
                    if (chatUpBean!!.private_chat && !chatUpBean.isplatinum) {
                        openPtVipBtn.setBackgroundResource(R.drawable.gradient_gold_vip)
                        datingTitle.text = "对方仅允许黄金会员报名"
                        datingContent.text = "对方仅允许黄金会员报名\n立即成为黄金会员，不要错过她"
                        openPtVipBtn.text = "成为黄金会员"
                        applyForDatingBtn.isVisible = false
                        openPtVipBtn.clickWithTrigger {
                            CommonFunction.startToVip(context1, VipPowerActivity.SOURCE_FREE_DATING)
                        }
                    } else {
                        //2.再判断有无次数
                        if (chatUpBean!!.residue_cnt <= 0) {
                            if (chatUpBean!!.isplatinum) {
                                datingTitle.text = "要报名活动吗"
                                datingContent.text = "今日免费聊天机会已用完"
                                applyForDatingBtn.isVisible = false
                                openPtVipBtn.setBackgroundResource(R.drawable.gradient_orange_15_bottom)
                                openPtVipBtn.text = "报名活动（${chatUpBean!!.dating_amount}糖果）"
                                openPtVipBtn.clickWithTrigger {
                                    datingApply()
                                }
                            } else {
                                openPtVipBtn.setBackgroundResource(R.drawable.gradient_gold_vip)
                                applyForDatingBtn.text = "报名活动（${chatUpBean!!.dating_amount}糖果）"
                                applyForDatingBtn.isVisible = true
                                datingTitle.text = "要报名活动吗"
                                datingContent.text = "用糖果证明活动诚意或许更好哦"
                                openPtVipBtn.text = "成为黄金会员，更多免费机会"
                                openPtVipBtn.clickWithTrigger {
                                    CommonFunction.startToVip(
                                        context1,
                                        VipPowerActivity.SOURCE_FREE_DATING
                                    )
                                }
                                applyForDatingBtn.clickWithTrigger {
                                    datingApply()
                                }
                            }

                        } else {
                            //3.报名约会
                            openPtVipBtn.setBackgroundResource(R.drawable.gradient_orange_15_bottom)
                            applyForDatingBtn.isVisible = false
                            datingTitle.text = "要报名活动吗"
                            datingContent.text =
                                "报名消耗一次聊天机会\n今日还有${chatUpBean!!.residue_cnt}次免费聊天机会"
                            openPtVipBtn.text = "报名活动"
                            openPtVipBtn.clickWithTrigger {
                                datingApply()
                            }
                        }
                    }
                }


            }

            TYPE_DATING_APPLYFOR_PRIVACY -> {
                GlideUtil.loadCircleImg(context1, datingBean?.avatar, datingAvator)
                openPtVipBtn.setBackgroundResource(R.drawable.gradient_gold_vip)
                datingTitle.text = "对方仅允许黄金会员报名"
                datingContent.text = "对方仅允许高级用户报名\n立即成为黄金会员，不要错过她"
                openPtVipBtn.text = "成为黄金会员"
                applyForDatingBtn.isVisible = false
                openPtVipBtn.clickWithTrigger {
                    CommonFunction.startToVip(context1, VipPowerActivity.SOURCE_FREE_DATING)
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

    fun datingApply() {
        val loadingDialog = LoadingDialog(context1)
        RetrofitFactory.instance.create(Api::class.java)
            .datingApply(UserManager.getSignParams(hashMapOf("dating_id" to datingBean!!.id)))
            .excute(object : BaseSubscriber<BaseResp<ApplyDatingBean?>>(null) {
                override fun onStart() {
                    super.onStart()
                    loadingDialog.show()
                }

                override fun onNext(t: BaseResp<ApplyDatingBean?>) {
                    super.onNext(t)
                    if (t.code == 200 && t.data != null) {
                        val attachment =
                            ChatDatingAttachment(
                                "${t.data!!.content}",
                                t.data!!.icon,
                                t.data!!.datingId
                            )
                        val message = MessageBuilder.createCustomMessage(
                            datingBean.accid,
                            SessionTypeEnum.P2P,
                            "",
                            attachment,
                            CustomMessageConfig()
                        )
                        NIMClient.getService(MsgService::class.java).sendMessage(message, false)
                            .setCallback(object : RequestCallback<Void?> {
                                override fun onSuccess(param: Void?) {
                                    EventBus.getDefault().post(UpdateApproveEvent())
                                    EventBus.getDefault().post(UpdateHiEvent())
                                    EventBus.getDefault().post(UpdateAccostListEvent())
                                    if (ActivityUtils.getTopActivity() !is ChatActivity) {
                                        Handler().postDelayed({
                                            loadingDialog.dismiss()
                                            ChatActivity.start(
                                                ActivityUtils.getTopActivity(),
                                                datingBean.accid
                                            )
                                        }, 600L)
                                    } else {
                                        EventBus.getDefault().post(UpdateSendGiftEvent(message))
                                        loadingDialog.dismiss()
                                    }
                                    dismiss()
                                }

                                override fun onFailed(code: Int) {
                                    loadingDialog.dismiss()
                                }

                                override fun onException(exception: Throwable) {
                                    loadingDialog.dismiss()
                                }
                            })

                    } else if (t.code == 419) {
                        loadingDialog.dismiss()
                        CommonFunction.gotoCandyRecharge(context1)
                        dismiss()
                    } else {
                        loadingDialog.dismiss()
                        CommonFunction.toast(t.msg)
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