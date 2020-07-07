package com.sdy.jitangapplication.common

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ToastUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.style.PictureCropParameterStyle
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.GiftStateBean
import com.sdy.jitangapplication.model.SendTipBean
import com.sdy.jitangapplication.model.UnlockCheckBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.activity.MessageInfoActivity
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.nim.attachment.SendCustomTipAttachment
import com.sdy.jitangapplication.ui.activity.*
import com.sdy.jitangapplication.ui.dialog.*
import com.sdy.jitangapplication.utils.GlideEngine
import com.sdy.jitangapplication.utils.UriUtils
import com.sdy.jitangapplication.utils.UserManager
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParseException

/**
 *    author : ZFM
 *    date   : 2019/7/2214:52
 *    desc   :
 *    version: 1.0
 */
object CommonFunction {
    /**
     * 验证视频介绍解锁
     * 400 错误toast
     * 201 冲会员
     * 222 （铂金会元/已经解锁视频 返回isnew_friend true是新好友 false 不是新建立 mv_url 视频地址 ）
     * 200 amount 糖果数 isplatinumvip 是否铂金会员
     */
    fun checkUnlockIntroduceVideo(context: Context, target_accid: String, gender: Int) {
        val loading = LoadingDialog(context)
        RetrofitFactory.instance.create(Api::class.java)
            .checkUnlockMv(UserManager.getSignParams(hashMapOf("target_accid" to target_accid)))
            .excute(object : BaseSubscriber<BaseResp<UnlockCheckBean?>>() {
                override fun onStart() {
                    super.onStart()
                    loading.show()
                }

                override fun onNext(t: BaseResp<UnlockCheckBean?>) {
                    super.onNext(t)
                    when (t.code) {
                        222 -> {//铂金会员解锁成功/已经解锁过了 isnew_friend 是否新好友
//                            if (t.data?.isnew_friend == true) {
//                                sendMatchFriendMessage(target_accid)
//                            }
                            PlayVideoDialog(context, t.data?.mv_url ?: "").show()
                        }
                        201 -> {
                            ChargeVipDialog(ChargeVipDialog.LOOK_VIDEO, context).show()
                        }
                        200 -> {//amount 解锁糖果 isplatinumvip 是否铂金会员true是 false不是
                            OpenPtVipDialog(
                                context,
                                OpenPtVipDialog.FROM_VIDEO_INTRODUCE,
                                t.data?.isplatinumvip == true,
                                t.data?.amount ?: 0,
                                target_accid, gender
                            ).show()
                        }
                        else -> {
                            toast(t.msg)
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


    /**
     * 不是会员先弹充值
     * 不是好友就赠送礼物
     * 是好友就直接跳聊天界面
     * 	201 拉起充值会员 206 是好友进聊天 200 拉起礼物列表
     */
    fun checkSendGift(context1: Context, target_accid: String) {
        val loadingDialog = LoadingDialog(context1)
        RetrofitFactory.instance.create(Api::class.java)
            .checkSendCandy(UserManager.getSignParams(hashMapOf("target_accid" to target_accid)))
            .excute(object : BaseSubscriber<BaseResp<Any?>>() {
                override fun onStart() {
                    super.onStart()
                    loadingDialog.show()
                }

                override fun onCompleted() {
                    super.onCompleted()
                    loadingDialog.dismiss()
                }

                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    when (t.code) {
                        200 -> {
                            SendGiftBeFriendDialog(target_accid, context1).show()
                        }
                        201 -> {//需要充值会员
                            ChargeVipDialog(ChargeVipDialog.INFINITE_CHAT, context1).show()
                        }
                        206 -> {//已经是好友了
                            ChatActivity.start(context1, target_accid)
                        }
                        207 -> {//女性对男性搭讪
                            //随机发送一条招呼文本消息
                            val msg = MessageBuilder.createTextMessage(
                                target_accid,
                                SessionTypeEnum.P2P,
                                t.msg
                            )
                            NIMClient.getService(MsgService::class.java).sendMessage(msg, false)
                                .setCallback(object : RequestCallback<Void> {
                                    override fun onSuccess(p0: Void?) {
                                        Handler().postDelayed({
                                            ChatActivity.start(context1, target_accid)
                                        }, 500L)
                                    }

                                    override fun onFailed(p0: Int) {
                                    }

                                    override fun onException(p0: Throwable?) {
                                    }

                                })
                        }
                        401 -> {//女性未认证
                            VerifyThenChatDialog(context1).show()
                        }
                    }
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    loadingDialog.dismiss()
                    if (e is BaseException) {
                        TickDialog(context1).show()
                    }
                }
            })
    }

    /**
     * 验证糖果解锁
     * 		400 toast错误
     * 		201 会员充值
     * 		222 （铂金会员解锁成功/已经解锁过了 isnew_friend 是否新好友）
     * 		200 amount 解锁糖果 isplatinumvip 是否铂金会员true是 false不是
     */
    fun checkUnlockContact(context: Context, target_accid: String, gender: Int) {
        if (!UserManager.isUserVip()) {
            ChargeVipDialog(ChargeVipDialog.GET_CONTACT, context).show()
        } else {
            val loading = LoadingDialog(context)
            RetrofitFactory.instance.create(Api::class.java)
                .checkUnlockContact(UserManager.getSignParams(hashMapOf("target_accid" to target_accid)))
                .excute(object : BaseSubscriber<BaseResp<UnlockCheckBean?>>() {
                    override fun onStart() {
                        super.onStart()
                        loading.show()
                    }

                    override fun onNext(t: BaseResp<UnlockCheckBean?>) {
                        super.onNext(t)
                        when (t.code) {
                            201 -> {
                                ChargeVipDialog(
                                    ChargeVipDialog.INFINITE_CHAT,
                                    context
                                ).show()
                            }
                            222 -> {//铂金会员解锁成功/已经解锁过了 isnew_friend 是否新好友
//                                if (t.data?.isnew_friend == true) {
//                                    sendMatchFriendMessage(target_accid)
//                                } else {
                                Handler().postDelayed({
                                    ChatActivity.start(context, target_accid)
                                }, 500L)

//                            }
                            }
                            200 -> {//amount 解锁糖果 isplatinumvip 是否铂金会员true是 false不是
                                OpenPtVipDialog(
                                    context,
                                    OpenPtVipDialog.FROM_CONTACT,
                                    t.data?.isplatinumvip == true,
                                    t.data?.amount ?: 0,
                                    target_accid, gender
                                ).show()
                            }
                            else -> {
                                toast(t.msg)
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
    }

    fun sendAccostTip(target_accid: String, tipContent: String = "回复对方消息自动领取搭讪礼物") {
        val attachment =
            SendCustomTipAttachment(tipContent, SendCustomTipAttachment.CUSTOME_TIP_NORMAL, false)
        val tip =
            MessageBuilder.createCustomMessage(target_accid, SessionTypeEnum.P2P, attachment)
        val config = CustomMessageConfig()
        config.enableUnreadCount = true
        config.enablePush = true
        tip.config = config
        NIMClient.getService(MsgService::class.java).sendMessage(tip, false)
    }


    private fun sendMatchFriendMessage(target_accid: String) {
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
                    ChatActivity.start(ActivityUtils.getTopActivity(), target_accid)
                }

                override fun onFailed(code: Int) {
                }

                override fun onException(exception: Throwable) {

                }
            })
    }


    /**
     * 打开礼物信封
     * attachment.getOrderId()
     *
     */
    fun openGiftLetter(
        isReceive: Boolean,
        giftStatus: Int,
        order_id: Int,
        context: Context,
        target_accid: String
    ) {
        val loadingDialog = LoadingDialog(context)
        RetrofitFactory.instance.create(Api::class.java)
            .checkGiftState(UserManager.getSignParams(hashMapOf("order_id" to order_id)))
            .excute(object : BaseSubscriber<BaseResp<GiftStateBean?>>(null) {
                override fun onStart() {
                    super.onStart()
                    loadingDialog.show()
                }

                override fun onNext(t: BaseResp<GiftStateBean?>) {
                    super.onNext(t)
                    loadingDialog.dismiss()
                    if (t.code == 200) {
                        ReceiveCandyGiftDialog(
                            isReceive,
                            giftStatus,
                            t.data!!,
                            order_id,
                            context, target_accid
                        ).show()
                    }
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    loadingDialog.dismiss()
                }
            })
    }

    /**
     * 助力成功发送tip消息
     */
    fun sendTips(target_accid: String, retTipsArr: MutableList<SendTipBean>) {
        for (tip in retTipsArr) {
            val attachment =
                SendCustomTipAttachment(tip.content, tip.showType, tip.ifSendUserShow)
            val tip =
                MessageBuilder.createCustomMessage(
                    target_accid,
                    SessionTypeEnum.P2P,
                    attachment
                )
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


    /**
     * 跳转到糖果充值
     */
    fun gotoCandyRecharge(context: Context) {
        context.startActivity<CandyRechargeActivity>()
//        RechargeCandyDialog(context).show()
    }

    fun dissolveRelationship(target_accid: String, negative: Boolean = false) {
        NIMClient.getService(MsgService::class.java)
            .deleteRecentContact2(target_accid, SessionTypeEnum.P2P)
        // 删除与某个聊天对象的全部消息记录
        //如果是被动删除，就删除会话
        NIMClient.getService(MsgService::class.java)
            .clearChattingHistory(target_accid, SessionTypeEnum.P2P)
//        NIMClient.getService(MsgService::class.java).clearServerHistory(target_accid, SessionTypeEnum.P2P)
        if (ActivityUtils.isActivityExistsInStack(ChatActivity::class.java))
            ActivityUtils.finishActivity(ChatActivity::class.java)
        if (ActivityUtils.isActivityExistsInStack(MatchDetailActivity::class.java))
            ActivityUtils.finishActivity(MatchDetailActivity::class.java)
        if (ActivityUtils.isActivityExistsInStack(MessageInfoActivity::class.java))
            ActivityUtils.finishActivity(MessageInfoActivity::class.java)
//        ActivityUtils.startActivity(MainActivity::class.java)

        //更新通讯录
        if (ActivityUtils.isActivityExistsInStack(ContactBookActivity::class.java))
            EventBus.getDefault().post(UpdateContactBookEvent())
    }


    /**
     * 微信登录
     */
    fun wechatLogin(context: Context, state: String) {
        val wxapi = WXAPIFactory.createWXAPI(context, null)
        wxapi.registerApp(Constants.WECHAT_APP_ID)
        if (!wxapi.isWXAppInstalled) {
            toast("你没有安装微信")
            return
        }
        val req = SendAuth.Req()
        req.scope = "snsapi_userinfo"
        req.state = state
        wxapi.sendReq(req)
//        UMShareAPI.get(this).getPlatformInfo(this, SHARE_MEDIA.WEIXIN, umAuthListener)
    }


    /**
     * 拍照或者选取照片
     */
    fun onTakePhoto(
        context: Context,
        maxCount: Int,
        requestCode: Int,
        chooseMode: Int = 1,
        compress: Boolean = false,
        showCamera: Boolean = true,
        rotateEnable: Boolean = false,
        cropEnable: Boolean = false,
        minSeconds: Int = -1,
        maxSeconds: Int = -1
    ) {
        PictureSelector.create(context as Activity)
            .openGallery(chooseMode)
            .maxSelectNum(maxCount)
            .minSelectNum(0)
            .imageSpanCount(4)
            .selectionMode(
                if (maxCount > 1) {
                    PictureConfig.MULTIPLE
                } else {
                    PictureConfig.SINGLE
                }
            )
            .isAndroidQTransform(true)//是否需要处理Android Q 拷贝至应用沙盒的操作
            .previewImage(true)
            .previewVideo(true)
            .isCamera(showCamera)
            .enableCrop(cropEnable)
            .compressSavePath(UriUtils.getCacheDir(context))
            .compress(compress)
            .videoMaxSecond(maxSeconds)
            .videoMinSecond(minSeconds)
            .minimumCompressSize(100)
            .scaleEnabled(true)
//            .showCropGrid(true)
//            .showCropFrame(true)
            .loadImageEngine(GlideEngine.createGlideEngine())// 自定义图片加载引擎
            .rotateEnabled(rotateEnable)
//            .cropImageWideHigh(4, 5)
            .withAspectRatio(4, 5)
            .compressSavePath(UriUtils.getCacheDir(context))
            .openClickSound(false)
            .forResult(requestCode)
    }

    /**
     * 单独拍照
     */
    fun openCamera(
        context: Context,
        requestCode: Int,
        chooseMode: Int = 1,
        compress: Boolean = false,
        rotateEnable: Boolean = false,
        cropEnable: Boolean = false
    ) {
        // 裁剪主题
        val mCropParameterStyle = PictureCropParameterStyle(
            ContextCompat.getColor(context, R.color.colorBlack),
            ContextCompat.getColor(context, R.color.colorBlack),
            ContextCompat.getColor(context, R.color.colorWhite),
            true
        )

        PictureSelector.create(context as Activity)
            .openCamera(chooseMode)
            .enableCrop(cropEnable)
            .rotateEnabled(rotateEnable)
            .setPictureCropStyle(mCropParameterStyle) // 动态自定义裁剪主题
            .theme(R.style.picture_default_style)
//            .cropImageWideHigh(4, 5)
            .withAspectRatio(4, 5)
            .isAndroidQTransform(true)//是否需要处理Android Q 拷贝至应用沙盒的操作
            .compressSavePath(UriUtils.getCacheDir(context))
            .compress(compress)
            .loadImageEngine(GlideEngine.createGlideEngine())// 自定义图片加载引擎
            .compressSavePath(UriUtils.getCacheDir(context))
            .forResult(requestCode)
    }


    fun startAnimation(firstView: View) {
        val aniTranslate =
            ObjectAnimator.ofFloat(
                firstView,
                "translationX",
                ScreenUtils.getScreenWidth().toFloat(),
                0F
            )
        val aniAlpha = ObjectAnimator.ofFloat(firstView, "alpha", 0F, 1F)
        val animSet = AnimatorSet()
        animSet.duration = 500L
        animSet.interpolator = OvershootInterpolator(1F)
        animSet.playTogether(aniTranslate, aniAlpha) //两个动画同时执行
        animSet.start()
        animSet.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {

            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }

        })

//        btnVerifyCode
    }

    fun payResultNotify(context: Context) {
        if (ActivityUtils.getTopActivity() is RegisterInfoActivity
            || ActivityUtils.getTopActivity() is GetRelationshipActivity
            || ActivityUtils.getTopActivity() is VerifyCodeActivity
        ) {//注册界面支付会员进入首页
            EventBus.getDefault().post(CloseDialogEvent())
        } else if (ActivityUtils.getTopActivity() is CandyProductDetailActivity) { //糖果充值更新糖果
            EventBus.getDefault().post(RefreshCandyMallDetailEvent())
        } else if (ActivityUtils.getTopActivity() is CandyMallActivity) { //糖果商城充值
            EventBus.getDefault().post(RefreshCandyMallEvent())
        } else if (ActivityUtils.getTopActivity() is MyCandyActivity) { //我的糖果界面充值
            EventBus.getDefault().post(RefreshMyCandyEvent(-1))
        } else if (ActivityUtils.getTopActivity() is AddLabelActivity) {//兴趣购买充值
            EventBus.getDefault().post(PayLabelResultEvent(true))
        } else if (ActivityUtils.getTopActivity() is MyLabelActivity) {//我的兴趣购买充值
            EventBus.getDefault().post(UpdateMyLabelEvent())
        } else if (ActivityUtils.getTopActivity() is IndexChoicenessActivity) {//购买置顶券
            EventBus.getDefault().post(UpdateTicketDataEvent())
        } else if (ActivityUtils.getTopActivity() is MainActivity) {
            EventBus.getDefault().post(RefreshEvent(true))
        } else {
            if (ActivityUtils.getTopActivity() !is MainActivity) {
                context.startActivity<MainActivity>()
            }
        }
        if (
            ActivityUtils.getTopActivity() !is RegisterInfoActivity
            && ActivityUtils.getTopActivity() !is GetRelationshipActivity
            && ActivityUtils.getTopActivity() !is VerifyCodeActivity
        ) {
            EventBus.getDefault().postSticky(RefreshEvent(true))
            EventBus.getDefault().postSticky(UserCenterEvent(true))
            EventBus.getDefault().post(CloseDialogEvent())
            EventBus.getDefault().post(RefreshTodayFateEvent())
        }


    }

    /**
     * 字符串 千位符
     *
     * @param num
     * @return
     */
    fun num2thousand(num: String): String {
        var numStr = "";
        val nf = NumberFormat.getInstance();
        try {
            val df = DecimalFormat("#,###");
            numStr = df.format(nf.parse(num));
        } catch (e: ParseException) {
            e.printStackTrace();
        }
        return numStr;
    }


    fun getErrorMsg(context: Context): String {
        return if (NetworkUtils.isConnected()) {
            context.getString(R.string.retry_load_error)
        } else {
            context.getString(R.string.retry_net_error)
        }
    }


    fun toast(msg: String) {
        ToastUtils.setBgColor(Color.parseColor("#80000000"))
        ToastUtils.setMsgColor(Color.WHITE)
        ToastUtils.setGravity(Gravity.CENTER, 0, 0)
        ToastUtils.showShort(msg)
    }


    fun longToast(msg: String) {
        ToastUtils.setBgColor(Color.parseColor("#80000000"))
        ToastUtils.setMsgColor(Color.WHITE)
        ToastUtils.setGravity(Gravity.CENTER, 0, 0)
        ToastUtils.showLong(msg)
    }


    fun startToFace(
        context: Context,
        type: Int = IDVerifyActivity.TYPE_ACCOUNT_NORMAL,
        requestCode: Int = -1
    ) {
        if (requestCode != -1)
            IDVerifyActivity.startActivityForResult(context as Activity, type, requestCode)
        else
            IDVerifyActivity.startActivity(context, type)
    }

    /**
     * 录制视频介绍
     */
    fun startToVideoIntroduce(
        context: Context,
        requestCode: Int = -1
    ) {
        VideoVerifyActivity1.start(context, requestCode)
    }


    fun initVideo(context: Context, gsyVideoPlayer: StandardGSYVideoPlayer, url: String) {
        gsyVideoPlayer.titleTextView.isVisible = false
        gsyVideoPlayer.backButton.isVisible = true
        gsyVideoPlayer.backButton.setImageResource(R.drawable.icon_close_transparent_video)
        gsyVideoPlayer.setIsTouchWiget(false)

        gsyVideoPlayer.setUp(url, true, "")
//        gsyVideoPlayer.startPlayLogic()
    }
}