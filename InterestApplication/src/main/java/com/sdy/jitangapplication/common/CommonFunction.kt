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
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.event.*
import com.sdy.jitangapplication.model.GreetTimesBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.nim.activity.MessageInfoActivity
import com.sdy.jitangapplication.ui.activity.ContactBookActivity
import com.sdy.jitangapplication.ui.activity.FindByTagListActivity
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
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

    fun commonGreet(
        context1: Context,
        target_accid: String,
        view: View? = null,
        position: Int = -1,
        targetAvator: String,
        needSwipe: Boolean = false
    ) {
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

        if (!NetworkUtils.isConnected()) {
            toast("请连接网络！")
            return
        }

        val params = UserManager.getBaseParams()
        params["target_accid"] = target_accid
        RetrofitFactory.instance.create(Api::class.java)
            .greet(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<GreetTimesBean?>>(null) {
                override fun onNext(t: BaseResp<GreetTimesBean?>) {
                    when {
                        t.code == 200 -> {//成功
                            if (!t.data?.default_msg.isNullOrEmpty()) {
                                val msg = MessageBuilder.createTextMessage(
                                    target_accid,
                                    SessionTypeEnum.P2P,
                                    t.data?.default_msg
                                )
                                NIMClient.getService(MsgService::class.java).sendMessage(msg, false)
                                    .setCallback(object : RequestCallback<Void> {
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
                                                    UpdateFindByTagListEvent(position, target_accid)
                                                )
                                            UserManager.saveLightingCount(UserManager.getLightingCount() - 1)
                                            EventBus.getDefault().post(UpdateHiCountEvent())
                                        }

                                        override fun onFailed(p0: Int) {
                                        }

                                        override fun onException(p0: Throwable?) {
                                        }

                                    })
                            }
                        }
                        t.code == 201 -> {//次数使用完毕，请充值次数
                            ChargeVipDialog(
                                ChargeVipDialog.DOUBLE_HI,
                                context1,
                                ChargeVipDialog.PURCHASE_GREET_COUNT
                            ).show()
                        }
                        t.code == 202 -> { //（该用户当日免费接收次数完毕，请充值会员获取）
                            GreetLimitlDialog(context1, targetAvator).show()
                        }
                        t.code == 203 -> { //招呼次数用完,认证获得次数
                            GreetUseUpDialog(
                                context1,
                                GreetUseUpDialog.GREET_USE_UP_VERIFY,
                                t.data
                            ).show()
                        }
                        t.code == 204 -> { //次数使用完毕，请充值会员获取次数
                            GreetUseUpDialog(
                                context1,
                                GreetUseUpDialog.GREET_USE_UP_CHARGEVIP,
                                t.data
                            ).show()
                        }
                        t.code == 205 -> { //会员次数用尽，明天再来
                            GreetUseUpDialog(
                                context1,
                                GreetUseUpDialog.GREET_USE_UP_TOMORROW
                            ).show()
                        }
                        t.code == 206 -> { //是好友/打过招呼的，直接跳转聊天界面
                            ChatActivity.start(context1, target_accid)
                        }
                        t.code == 401 -> { // 发起招呼失败,对方开启了招呼认证,您需要通过人脸认证
                            HarassmentDialog(context1, HarassmentDialog.CHATHI).show() //开启招呼提示
                        }
                        t.code == 403 -> //登录异常
                            UserManager.startToLogin(context1 as Activity)
                        else -> {
                            toast(t.msg)
                        }
                    }

                    view?.isEnabled = true
                }

                override fun onError(e: Throwable?) {
                    toast(context1.getString(R.string.service_error))

                }
            })
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
    fun openCamera(
        context: Context,
        requestCode: Int,
        chooseMode: Int = 1,
        compress: Boolean = false
    ) {
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