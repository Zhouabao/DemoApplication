package com.sdy.jitangapplication.common

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
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
import com.sdy.jitangapplication.model.GreetCheckBean
import com.sdy.jitangapplication.model.GreetTimesBean
import com.sdy.jitangapplication.model.SendTipBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.activity.MessageInfoActivity
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.nim.attachment.SendCustomTipAttachment
import com.sdy.jitangapplication.ui.activity.*
import com.sdy.jitangapplication.ui.dialog.*
import com.sdy.jitangapplication.utils.GlideEngine
import com.sdy.jitangapplication.utils.UriUtils
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CommonAlertDialog
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

    /**
     * 打招呼
     * code  201  次数使用完毕，请充值次数
     * code  202  你就弹框（该用户当日免费接收次数完毕，请充值会员获取）
     * code  203  招呼次数用完,认证获得次数
     * 204  次数使用完毕，请充值会员获取次数
     * 205  今日次数使用完毕
     * 206  是好友/打过招呼的
     * code  401  发起招呼失败,对方开启了招呼认证,您需要通过人脸认证
     * code  400  招呼次数用尽~
     */
    //    206  是好友或者有效招呼    202  需要充值会员  401  需要人脸认证   普通400 错误信息

    fun commonGreet(
        context1: Context,
        target_accid: String,
        view: View? = null,
        position: Int = -1,
        targetAvator: String,
        needSwipe: Boolean = false
    ) {

        if (!NetworkUtils.isConnected()) {
            toast("请连接网络！")
            return
        }

        val loadingDialog = LoadingDialog(context1)
        RetrofitFactory.instance.create(Api::class.java)
            .checkGreetStateCandy(UserManager.getSignParams(hashMapOf("target_accid" to target_accid)))
            .excute(object : BaseSubscriber<BaseResp<GreetCheckBean?>>() {
                override fun onStart() {
                    super.onStart()
                    loadingDialog.show()
                }

                override fun onCompleted() {
                    super.onCompleted()
                    loadingDialog.dismiss()
                }

                override fun onNext(t: BaseResp<GreetCheckBean?>) {
                    super.onNext(t)
                    if (loadingDialog.isShowing)
                        loadingDialog.dismiss()
                    when (t.code) {
                        200 -> {
                            if (t.data != null && t.data!!.type != -1) {
                                when (t.data!!.type!!) {
                                    1 -> {// 免费
                                        greet(
                                            target_accid,
                                            view,
                                            context1,
                                            needSwipe,
                                            position,
                                            targetAvator
                                        )
                                    }
                                    2 -> {//消耗糖果打招呼
                                        CommonAlertDialog.Builder(context1)
                                            .setTitle("确认打招呼")
                                            .setContent("每次打招呼将消耗${t.data!!.greet_amount}点糖果\n若对方24小时未回复将退回糖果")
                                            .setCancelAble(true)
                                            .setCancelIconIsVisibility(true)
                                            .setCancelText("取消")
                                            .setConfirmText("继续打招呼")
                                            .setOnConfirmListener(object :
                                                CommonAlertDialog.OnConfirmListener {
                                                override fun onClick(dialog: Dialog) {
                                                    dialog.dismiss()
                                                    greet(
                                                        target_accid,
                                                        view,
                                                        context1,
                                                        needSwipe,
                                                        position,
                                                        targetAvator
                                                    )

                                                }
                                            })
                                            .setOnCancelListener(object :
                                                CommonAlertDialog.OnCancelListener {
                                                override fun onClick(dialog: Dialog) {
                                                    dialog.dismiss()
                                                }
                                            })
                                            .create()
                                            .show()

                                    }
                                    3 -> {//有今日意愿选项打招呼
                                        HasWantRreetDialog(
                                            context1,
                                            target_accid,
                                            targetAvator,
                                            needSwipe,
                                            position,
                                            t.data!!.nickname,
                                            t.data!!.greet_amount,
                                            t.data!!.mycandy_amount,
                                            t.data!!.goodswish
                                        ).show()
                                    }
                                }
                            }

                        }

                        206 -> {//是好友或者有效招呼
                            ChatActivity.start(context1, target_accid)
                        }
                        202 -> {//需要充值会员
                            OpenVipDialog(
                                context1,
                                from = OpenVipDialog.FROM_NEAR_CHAT_GREET,
                                peopleAmount = t.data!!.people_amount
                            ).show()
                        }
                        401 -> {//需要人脸认证(对方有意愿)
                            CommonAlertDialog.Builder(context1)
                                .setTitle("对方设置了今日意愿")
                                .setContent("通过认证后可以与对方获得联系\n一起做他想做的事")
                                .setCancelIconIsVisibility(true)
                                .setCancelAble(false)
                                .setCancelText("取消")
                                .setConfirmText("立即认证")
                                .setOnCancelListener(object : CommonAlertDialog.OnCancelListener {
                                    override fun onClick(dialog: Dialog) {
                                        dialog.dismiss()
                                    }
                                })
                                .setOnConfirmListener(object : CommonAlertDialog.OnConfirmListener {
                                    override fun onClick(dialog: Dialog) {
                                        context1.startActivity<IDVerifyActivity>()
                                        dialog.dismiss()
                                    }
                                })
                                .create()
                                .show()
                        }

                        402 -> {//需要人脸认证(对方无意愿)
                            CommonAlertDialog.Builder(context1)
                                .setTitle("您需要认证后才能打招呼")
                                .setContent("本平台为真人社交平台，为保证用户真实性\n您先需要进行人脸验证")
                                .setCancelIconIsVisibility(true)
                                .setCancelAble(false)
                                .setCancelText("取消")
                                .setConfirmText("立即认证")
                                .setOnCancelListener(object : CommonAlertDialog.OnCancelListener {
                                    override fun onClick(dialog: Dialog) {
                                        dialog.dismiss()
                                    }
                                })
                                .setOnConfirmListener(object : CommonAlertDialog.OnConfirmListener {
                                    override fun onClick(dialog: Dialog) {
                                        context1.startActivity<IDVerifyActivity>()
                                        dialog.dismiss()
                                    }
                                })
                                .create()
                                .show()
                        }
                        400 -> {//错误信息
                            toast(t.msg)
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


    fun greet(
        target_accid: String,
        view: View?,
        context1: Context,
        needSwipe: Boolean,
        position: Int,
        targetAvator: String
    ) {
        val loadingDialog = LoadingDialog(context1)
        val params = UserManager.getBaseParams()
        params["target_accid"] = target_accid
        RetrofitFactory.instance.create(Api::class.java)
            .greet(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<GreetTimesBean?>>(null) {
                override fun onStart() {
                    super.onStart()
                    loadingDialog.show()
                }

                override fun onCompleted() {
                    super.onCompleted()
                    loadingDialog.dismiss()
                }

                override fun onNext(t: BaseResp<GreetTimesBean?>) {
                    when (t.code) {
                        200 -> {//成功
                            if (!t.data?.default_msg.isNullOrEmpty()) {
                                //发送招呼消息
                                val chatHiAttachment =
                                    ChatHiAttachment(
                                        ChatHiAttachment.CHATHI_HI
                                    )
                                val config = CustomMessageConfig()
                                config.enableUnreadCount = false
                                config.enablePush = false
                                val message =
                                    MessageBuilder.createCustomMessage(
                                        target_accid,
                                        SessionTypeEnum.P2P,
                                        "",
                                        chatHiAttachment,
                                        config
                                    )
                                NIMClient.getService(MsgService::class.java)
                                    .sendMessage(message, false)

                                //随机发送一条招呼文本消息
                                val msg =
                                    MessageBuilder.createTextMessage(
                                        target_accid,
                                        SessionTypeEnum.P2P,
                                        t.data?.default_msg
                                    )
                                val params =
                                    hashMapOf<String, Any>("needCandyImg" to false)
                                msg.remoteExtension = params
                                NIMClient.getService(MsgService::class.java)
                                    .sendMessage(msg, false)
                                    .setCallback(object :
                                        RequestCallback<Void> {
                                        override fun onSuccess(p0: Void?) {
                                            view?.postDelayed({
                                                ChatActivity.start(
                                                    context1,
                                                    target_accid
                                                )
                                            }, 500L)
                                            if (needSwipe)
                                                EventBus.getDefault().post(
                                                    GreetTopEvent(
                                                        context1,
                                                        true,
                                                        target_accid
                                                    )
                                                )
                                            //刷新对方用户信息页面
                                            if (ActivityUtils.isActivityExistsInStack(
                                                    MatchDetailActivity::class.java
                                                )
                                            )
                                                EventBus.getDefault().post(
                                                    GreetDetailSuccessEvent(
                                                        true
                                                    )
                                                )
                                            //刷新兴趣找人列表
                                            if (ActivityUtils.isActivityExistsInStack(
                                                    FindByTagListActivity::class.java
                                                )
                                            )
                                                EventBus.getDefault().post(
                                                    UpdateFindByTagListEvent(
                                                        position,
                                                        target_accid
                                                    )
                                                )
                                            UserManager.saveLightingCount(
                                                UserManager.getLightingCount() - 1
                                            )
                                            EventBus.getDefault()
                                                .post(
                                                    UpdateHiCountEvent()
                                                )
                                        }

                                        override fun onFailed(p0: Int) {
                                        }

                                        override fun onException(p0: Throwable?) {
                                        }

                                    })
                            }
                        }
                        201 -> {//次数使用完毕，请充值次数
                            ChargeVipDialog(
                                ChargeVipDialog.DOUBLE_HI,
                                context1,
                                ChargeVipDialog.PURCHASE_GREET_COUNT
                            ).show()
                        }
                        202 -> { //（该用户当日免费接收次数完毕，请充值会员获取）
                            GreetLimitlDialog(
                                context1,
                                targetAvator
                            ).show()
                        }
                        203 -> { //招呼次数用完,认证获得次数
                            GreetUseUpDialog(
                                context1,
                                GreetUseUpDialog.GREET_USE_UP_VERIFY,
                                t.data
                            ).show()
                        }
                        204 -> { //次数使用完毕，请充值会员获取次数
                            GreetUseUpDialog(
                                context1,
                                GreetUseUpDialog.GREET_USE_UP_CHARGEVIP,
                                t.data
                            ).show()
                        }
                        205 -> { //会员次数用尽，明天再来
                            GreetUseUpDialog(
                                context1,
                                GreetUseUpDialog.GREET_USE_UP_TOMORROW
                            ).show()
                        }
                        206 -> { //是好友/打过招呼的，直接跳转聊天界面
                            ChatActivity.start(
                                context1,
                                target_accid
                            )
                        }
                        401 -> { // 发起招呼失败,对方开启了招呼认证,您需要通过人脸认证
                            HarassmentDialog(
                                context1,
                                HarassmentDialog.CHATHI
                            ).show() //开启招呼提示
                        }
                        403 -> { //登录异常
                            UserManager.startToLogin(context1 as Activity)
                        }
                        419 -> {
                            AlertCandyEnoughDialog(
                                context1,
                                AlertCandyEnoughDialog.FROM_SEND_GIFT
                            ).show()
                        }
                        else -> {
                            toast(t.msg)
                        }
                    }
                }

                override fun onError(e: Throwable?) {
                    loadingDialog.dismiss()
                    if (e is BaseException)
                        TickDialog(context1).show()
                    else
                        toast(context1.getString(R.string.service_error))

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

        EventBus.getDefault().postSticky(UpdateHiEvent())
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
        cropEnable: Boolean = false
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
            context.startActivity<MainActivity>()
        } else if (ActivityUtils.getTopActivity() is CandyProductDetailActivity) { //糖果充值更新糖果
            EventBus.getDefault().post(RefreshCandyMallDetailEvent())
        } else if (ActivityUtils.getTopActivity() is CandyMallActivity) { //糖果商城充值
            EventBus.getDefault().post(RefreshCandyMallEvent())
        } else if (ActivityUtils.getTopActivity() is MyCandyActivity) { //我的糖果界面充值
            EventBus.getDefault().post(RefreshMyCandyEvent(-1))
        } else if (ActivityUtils.getTopActivity() is AddLabelActivity) {
            EventBus.getDefault().post(PayLabelResultEvent(true))
        } else if (ActivityUtils.getTopActivity() is MyLabelActivity) {
            EventBus.getDefault().post(UpdateMyLabelEvent())
        } else if (ActivityUtils.getTopActivity() is MainActivity) {
            EventBus.getDefault().post(RefreshEvent(true))
        } else {
            if (ActivityUtils.getTopActivity() !is MainActivity) {
                context.startActivity<MainActivity>()
            }
        }
        if (
            ActivityUtils.getTopActivity() !is RegisterInfoActivity
            && ActivityUtils.getTopActivity() !is RegisterInfoActivity
            && ActivityUtils.getTopActivity() !is VerifyCodeActivity
        ) {
            EventBus.getDefault().postSticky(RefreshEvent(true))
            EventBus.getDefault().postSticky(UserCenterEvent(true))
            EventBus.getDefault().post(CloseDialogEvent())
        }


    }

}