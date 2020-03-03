package com.sdy.jitangapplication.common

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ToastUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.rx.BaseSubscriber
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.event.GreetEvent
import com.sdy.jitangapplication.event.UpdateHiEvent
import com.sdy.jitangapplication.event.UpdateLikeMeReceivedEvent
import com.sdy.jitangapplication.model.GreetTimesBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.attachment.ChatHiAttachment
import com.sdy.jitangapplication.ui.activity.GreetReceivedActivity
import com.sdy.jitangapplication.ui.activity.MainActivity
import com.sdy.jitangapplication.ui.dialog.ChargeVipDialog
import com.sdy.jitangapplication.ui.dialog.GreetLimitlDialog
import com.sdy.jitangapplication.ui.dialog.GreetUseUpDialog
import com.sdy.jitangapplication.ui.dialog.HarassmentDialog
import com.sdy.jitangapplication.utils.GlideEngine
import com.sdy.jitangapplication.utils.UriUtils
import com.sdy.jitangapplication.utils.UserManager
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import org.greenrobot.eventbus.EventBus

/**
 *    author : ZFM
 *    date   : 2019/7/2214:52
 *    desc   :
 *    version: 1.0
 */
object CommonFunction {

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
     * 打招呼通用逻辑
     */
    fun commonGreet(context: Context, targetAccid: String, view: View? = null) {
        if (view != null)
            view.isEnabled = false
        /**
         * 判断当前能否打招呼
         */
        if (!NetworkUtils.isConnected()) {
            toast("请连接网络！")
            if (view != null)
                view.isEnabled = true
            return
        }

        greet(targetAccid, context, view)
    }


    /**
     * 打招呼
     * code  201  次数使用完毕，请充值次数
     * code  202  你就弹框（该用户当日免费接收次数完毕，请充值会员获取）
     * code  203  招呼次数用完,认证获得次数
     * 204  次数使用完毕，请充值会员获取次数
     * 205  今日次数使用完毕
     * code  401  发起招呼失败,对方开启了招呼认证,您需要通过人脸认证
     * code  400  招呼次数用尽~
     */
    fun greet(target_accid: String, context1: Context, view: View?) {
        if (!NetworkUtils.isConnected()) {
            toast("请连接网络！")
            return
        }

        val params = UserManager.getBaseParams()
        params["tag_id"] = UserManager.getGlobalLabelId()
        params["target_accid"] = target_accid
        RetrofitFactory.instance.create(Api::class.java)
            .greet(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<GreetTimesBean?>>(null) {
                override fun onNext(t: BaseResp<GreetTimesBean?>) {
                    when {
                        t.code == 200 -> {//成功
                            val chatHiAttachment = ChatHiAttachment(
                                UserManager.getGlobalLabelName(),
                                ChatHiAttachment.CHATHI_HI
                            )
                            val config = CustomMessageConfig()
                            config.enableUnreadCount = false
                            config.enablePush = false
                            val message = MessageBuilder.createCustomMessage(
                                target_accid,
                                SessionTypeEnum.P2P,
                                "",
                                chatHiAttachment,
                                config
                            )
                            NIMClient.getService(MsgService::class.java).sendMessage(message, false)
                                .setCallback(object :
                                    RequestCallback<Void?> {
                                    override fun onSuccess(param: Void?) {
                                        //发送通知修改招呼次数
                                        UserManager.saveLightingCount(UserManager.getLightingCount() - 1)
                                        if (ActivityUtils.isActivityAlive(GreetReceivedActivity::class.java.newInstance())) {
                                            EventBus.getDefault().post(UpdateLikeMeReceivedEvent())
                                        }
                                        ChatActivity.start(context1, target_accid)
                                    }

                                    override fun onFailed(code: Int) {

                                    }

                                    override fun onException(exception: Throwable) {

                                    }
                                })
                        }
                        t.code == 201 -> {//次数使用完毕，请充值次数
                            ChargeVipDialog(
                                ChargeVipDialog.DOUBLE_HI,
                                context1,
                                ChargeVipDialog.PURCHASE_GREET_COUNT
                            ).show()
                            EventBus.getDefault().post(GreetEvent(context1, false))
                        }
                        t.code == 202 -> { //（该用户当日免费接收次数完毕，请充值会员获取）
                            GreetLimitlDialog(context1).show()
                            EventBus.getDefault().post(GreetEvent(context1, false))
                        }
                        t.code == 203 -> { //招呼次数用完,认证获得次数
                            GreetUseUpDialog(context1, GreetUseUpDialog.GREET_USE_UP_VERIFY, t.data).show()
                            EventBus.getDefault().post(GreetEvent(context1, false))
                        }
                        t.code == 204 -> { //次数使用完毕，请充值会员获取次数
                            GreetUseUpDialog(context1, GreetUseUpDialog.GREET_USE_UP_CHARGEVIP, t.data).show()
                            EventBus.getDefault().post(GreetEvent(context1, false))
                        }
                        t.code == 205 -> { //会员次数用尽，明天再来
                            EventBus.getDefault().post(GreetEvent(context1, false))
                            GreetUseUpDialog(context1, GreetUseUpDialog.GREET_USE_UP_TOMORROW).show()
                        }
                        t.code == 401 -> { // 发起招呼失败,对方开启了招呼认证,您需要通过人脸认证
                            EventBus.getDefault().post(GreetEvent(context1, false))
                            HarassmentDialog(context1, HarassmentDialog.CHATHI).show() //开启招呼提示
                        }
                        t.code == 403 -> //登录异常
                            UserManager.startToLogin(context1 as Activity)
                        else -> {
                            EventBus.getDefault().post(GreetEvent(context1, false))
                            toast(t.msg)
                        }
                    }

                    view?.isEnabled = true
                }

                override fun onError(e: Throwable?) {
                    toast(context1.getString(R.string.service_error))
                    EventBus.getDefault().post(GreetEvent(context1, false))

                }
            })
    }


    fun dissolveRelationship(target_accid: String, negative: Boolean = false) {
        NIMClient.getService(MsgService::class.java).deleteRecentContact2(target_accid, SessionTypeEnum.P2P)
        // 删除与某个聊天对象的全部消息记录
        //如果不是被动删除，就删除会话
        if (!negative) {
            NIMClient.getService(MsgService::class.java).clearChattingHistory(target_accid, SessionTypeEnum.P2P)
            ActivityUtils.finishAllActivities()
            ActivityUtils.startActivity(MainActivity::class.java)
        } else {
            EventBus.getDefault().postSticky(UpdateHiEvent())
        }
//        EventBus.getDefault().post(UpdateContactBookEvent())
//        if (AppUtils.isAppForeground() && ActivityUtils.isActivityAlive(MessageInfoActivity::class.java.newInstance()))
//            ActivityUtils.finishActivity(MessageInfoActivity::class.java)
//        if (AppUtils.isAppForeground() && ActivityUtils.isActivityAlive(ChatActivity::class.java.newInstance()))
//            ActivityUtils.finishActivity(ChatActivity::class.java)
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
        showCamera: Boolean = true
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
            .enableCrop(false)
            .compressSavePath(UriUtils.getCacheDir(context))
            .compress(compress)
            .minimumCompressSize(100)
            .scaleEnabled(true)
            .showCropFrame(true)
            .loadImageEngine(GlideEngine.createGlideEngine())// 自定义图片加载引擎
            .rotateEnabled(false)
            .withAspectRatio(9, 16)
            .compressSavePath(UriUtils.getCacheDir(context))
            .openClickSound(false)
            .forResult(requestCode)
    }

    /**
     * 单独拍照
     */
    fun openCamera(context: Context, requestCode: Int, chooseMode: Int = 1, compress: Boolean = false) {
        PictureSelector.create(context as Activity)
            .openCamera(chooseMode)
            .enableCrop(false)
            .isAndroidQTransform(true)//是否需要处理Android Q 拷贝至应用沙盒的操作
            .compressSavePath(UriUtils.getCacheDir(context))
            .compress(compress)
            .loadImageEngine(GlideEngine.createGlideEngine())// 自定义图片加载引擎
            .compressSavePath(UriUtils.getCacheDir(context))
            .forResult(requestCode)
    }

}