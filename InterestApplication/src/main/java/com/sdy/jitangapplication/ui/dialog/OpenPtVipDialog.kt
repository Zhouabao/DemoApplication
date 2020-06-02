package com.sdy.jitangapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ActivityUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
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
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.event.CloseDialogEvent
import com.sdy.jitangapplication.event.MatchByWishHelpEvent
import com.sdy.jitangapplication.model.*
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.activity.VipPowerActivity
import com.sdy.jitangapplication.ui.adapter.VipChargeAdapter
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CommonAlertDialog
import kotlinx.android.synthetic.main.dialog_open_pt_vip.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.startActivity

/**
 *    author : ZFM
 *    date   : 2020/5/99:45
 *    desc   :充值成为铂金会员
 *    version: 1.0
 */
class OpenPtVipDialog(
    val context1: Context,
    var from: Int = FROM_CONTACT,
    var isPlatniumVip: Boolean = false,//是否是珀金会员
    var amount: Int,
    var target_accid: String,
    var gender: Int = 0
) :
    Dialog(context1, R.style.MyDialog) {


    companion object {
        const val FROM_CONTACT = 1//从获取联系方式
        const val FROM_VIDEO_INTRODUCE = 2//视频介绍
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_open_pt_vip)
        initWindow()
        initView()
        if (!isPlatniumVip)
            productLists()
        EventBus.getDefault().register(this)
    }

    private fun initView() {
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        if (from == FROM_CONTACT) {
            openPtvipLogo.setImageResource(R.drawable.icon_vip_pt_contact)
            openPtVipTitle.text = "查看对方联系方式需要\n成为钻石会员或糖果解锁"
        } else {
            openPtvipLogo.setImageResource(R.drawable.icon_vip_pt_video)
            openPtVipTitle.text = "查看对方的真人视频介绍需要\n成为会员或糖果解锁"
        }
        openPtVipBtn.isVisible = !isPlatniumVip
        candyUnlockBtn.text = "${amount}糖果解锁"

        candyUnlockBtn.clickWithTrigger {
            confirmUnlockContact()
        }

        openPtVipBtn.clickWithTrigger {
            context1.startActivity<VipPowerActivity>("type" to VipPowerBean.TYPE_PT_VIP)
        }

    }

    private fun initWindow() {
        val window = this.window
        window?.setGravity(Gravity.BOTTOM)
//        window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val params = window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
//        params?.windowAnimations = R.style.MyDialogBottomAnimation
//        params?.y = SizeUtils.dp2px(20F)

        window?.attributes = params

    }

    /**
     * 二次确认是否解锁
     */
    fun confirmUnlockContact() {
        dismiss()
        CommonAlertDialog.Builder(context1)
            .setTitle("解锁提示")
            .setContent(
                "确认以${amount}糖果购买${if (gender == 1) {
                    "他"
                } else {
                    "她"
                }}的${if (from == FROM_CONTACT) {
                    "联系方式"
                } else {
                    "视频介绍"
                }}"
            )
            .setConfirmText("确认购买")
            .setOnConfirmListener(object : CommonAlertDialog.OnConfirmListener {
                override fun onClick(dialog: Dialog) {
                    dialog.dismiss()
                    if (from == FROM_CONTACT)
                        unlockContact()
                    else
                        unlockVideo()
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


    /**
     * 解锁联系方式
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
//                        if (t.data!!.isnew_friend) {
//                            sendMatchFriendMessage(loading)
//                        } else {
                        loading.dismiss()
                        if (ActivityUtils.getTopActivity() is MatchDetailActivity)
                            EventBus.getDefault().post(MatchByWishHelpEvent(true, target_accid))
                        if (ActivityUtils.getTopActivity() !is ChatActivity)
                            ChatActivity.start(ActivityUtils.getTopActivity(), target_accid)
//                        }
                    } else if (t.code == 222) { //已经解锁过
                        loading.dismiss()
                        if (ActivityUtils.getTopActivity() !is ChatActivity)
                            ChatActivity.start(ActivityUtils.getTopActivity(), target_accid)
                    } else if (t.code == 419) {
                        loading.dismiss()
                        RechargeCandyDialog(context).show()
                    } else {
                        loading.dismiss()
                    }

                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    loading.dismiss()
                }
            })
    }


    /**
     * 解锁视频介绍
     * 	400 toast错误
     * 	201 充值会员
     * 	419 糖果余额不足
     * 	200 成功 mv_url 视频地址 isnew_friend是否新建立好友 true是 false不是
     */
    fun unlockVideo() {
        val loading = LoadingDialog(context1)
        RetrofitFactory.instance.create(Api::class.java)
            .unlockMv(UserManager.getSignParams(hashMapOf("target_accid" to target_accid)))
            .excute(object : BaseSubscriber<BaseResp<UnlockCheckBean?>>() {
                override fun onStart() {
                    super.onStart()
                    loading.show()
                }

                override fun onNext(t: BaseResp<UnlockCheckBean?>) {
                    super.onNext(t)
                    when (t.code) {
                        200 -> {
                            PlayVideoDialog(context, t.data?.mv_url ?: "").show()
                        }
                        201 -> { //已经解锁过
                            ChargeVipDialog(ChargeVipDialog.LOOK_VIDEO, context1).show()
                        }
                        419 -> {
                            AlertCandyEnoughDialog(
                                context1,
                                AlertCandyEnoughDialog.FROM_SEND_GIFT
                            ).show()
                        }
                        else -> {
                            CommonFunction.toast(t.msg)
                        }
                    }

                }

                override fun onCompleted() {
                    super.onCompleted()
                    loading.dismiss()
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    loading.dismiss()
                }
            })
    }


    private fun sendMatchFriendMessage(loadingDialog: LoadingDialog) {
        val wishHelpFirendAttachment = ChatHiAttachment(ChatHiAttachment.CHATHI_MATCH)
        val config = CustomMessageConfig()
        config.enablePush = false
        val message = MessageBuilder.createCustomMessage(
            target_accid,
            SessionTypeEnum.P2P,
            "",
            wishHelpFirendAttachment,
            config
        )

        NIMClient.getService(MsgService::class.java).sendMessage(message, false)
            .setCallback(object : RequestCallback<Void?> {
                override fun onSuccess(param: Void?) {
                    if (ActivityUtils.getTopActivity() is MatchDetailActivity)
                        EventBus.getDefault().post(MatchByWishHelpEvent(true, target_accid))
                    if (ActivityUtils.getTopActivity() !is ChatActivity) {
                        Thread.sleep(750L)
                        loadingDialog.dismiss()
                        ChatActivity.start(ActivityUtils.getTopActivity(), target_accid)
                    } else {
                        loadingDialog.dismiss()
                    }
//                    sendContactMessage(contactContent, contactWay)
                }

                override fun onFailed(code: Int) {
                    loadingDialog.dismiss()
                }

                override fun onException(exception: Throwable) {
                    loadingDialog.dismiss()
                }
            })
    }


    /**
     * 请求支付方式
     */
    fun productLists() {
        RetrofitFactory.instance.create(Api::class.java)
            .getThreshold(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<ChargeWayBeans?>>(null) {
                override fun onNext(it: BaseResp<ChargeWayBeans?>) {
                    if (it.code == 200) {
                        if (it.data != null) {
                            chargeWayBeans = it.data
                            setPurchaseType()
                            payways.addAll(chargeWayBeans!!.paylist ?: mutableListOf())
                        }
                    } else {
                        CommonFunction.toast(it.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e != null && e is BaseException) {
                        TickDialog(context).show()
                    }
                }
            })
    }


    private var chargeWayBeans: ChargeWayBeans? = null
    private var payways: MutableList<PaywayBean> = mutableListOf()


    private val vipChargeAdapter by lazy { VipChargeAdapter() }
    private fun setPurchaseType() {
        //判断是否有选中推荐的，没有的话就默认选中第一个价格。
        var ispromote = false
        for (charge in chargeWayBeans!!.list ?: mutableListOf()) {
            if (charge.is_promote) {
                ispromote = true
                break
            }
        }
        if (!ispromote && (chargeWayBeans!!.list ?: mutableListOf()).isNotEmpty()) {
            chargeWayBeans!!.list!![0].is_promote = true
        }
        vipChargeAdapter.setNewData(chargeWayBeans!!.list)
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


}