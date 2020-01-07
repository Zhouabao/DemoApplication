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
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.event.GreetEvent
import com.sdy.jitangapplication.model.GreetBean
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.ui.activity.MainActivity
import com.sdy.jitangapplication.ui.dialog.ChargeVipDialog
import com.sdy.jitangapplication.ui.dialog.HarassmentDialog
import com.sdy.jitangapplication.ui.dialog.SayHiDialog
import com.sdy.jitangapplication.ui.dialog.TickDialog
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
    fun commonGreet(
        context: Context,
        isfriend: Boolean,
        greet_switch: Boolean,//接收招呼开关   true  接收招呼      false   不接受招呼
        greet_state: Boolean,// 认证招呼开关   true  开启认证      flase   不开启认证
        targetAccid: String,
        targetNickName: String,
        isgreeted: Boolean = false,//招呼是否有效
        view: View
    ) {
        view.isEnabled = false
        /**
         * 判断当前能否打招呼
         */
        if (!NetworkUtils.isConnected()) {
            toast("请连接网络！")
            view.isEnabled = true
            return
        }

        val params = UserManager.getBaseParams()
        params["target_accid"] = targetAccid
        RetrofitFactory.instance.create(Api::class.java)
            .greetState(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<GreetBean?>>(null) {
                override fun onStart() {
                }

                override fun onNext(t: BaseResp<GreetBean?>) {
                    if (t.code == 200) {
                        val greetBean = t.data
                        if (greetBean != null && greetBean.lightningcnt != -1) {
                            if (greetBean.isfriend || greetBean.isgreet) {
                                ChatActivity.start(context, targetAccid)
                            } else {
                                if (UserManager.getLightingCount() > 0) {
                                    if (!greet_switch) {
                                        toast("对方已关闭招呼功能")
                                    } else {
                                        if (greet_state && UserManager.isUserVerify() != 1) {
                                            HarassmentDialog(context, HarassmentDialog.CHATHI).show()
                                        } else {
                                            SayHiDialog(targetAccid, targetNickName, context).show()
                                        }
                                    }
                                } else {
                                    ChargeVipDialog(
                                        ChargeVipDialog.DOUBLE_HI,
                                        context,
                                        ChargeVipDialog.PURCHASE_GREET_COUNT
                                    ).show()
                                }
                            }

                        } else {
                            toast(t.msg)
                        }
                    } else {
                        EventBus.getDefault().post(GreetEvent(context, true))
                        toast(t.msg)
                    }
                    view.postDelayed({ view.isEnabled = true }, 500L)
                }

                override fun onError(e: Throwable?) {
                    view.isEnabled = true
                    if (e is BaseException) {
                        TickDialog(context).show()
                    }
                }
            })


    }


    fun dissolveRelationship(target_accid: String, negative: Boolean = false) {
        NIMClient.getService(MsgService::class.java).deleteRecentContact2(target_accid, SessionTypeEnum.P2P)
        // 删除与某个聊天对象的全部消息记录
        if (!negative)
            NIMClient.getService(MsgService::class.java).clearChattingHistory(target_accid, SessionTypeEnum.P2P)
        ActivityUtils.finishAllActivities()
        ActivityUtils.startActivity(MainActivity::class.java)
//        EventBus.getDefault().post(UpdateContactBookEvent())
//        EventBus.getDefault().post(UpdateHiEvent())
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
            .isAndroidQTransform(false)//是否需要处理Android Q 拷贝至应用沙盒的操作
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
            .isAndroidQTransform(false)//是否需要处理Android Q 拷贝至应用沙盒的操作
            .compressSavePath(UriUtils.getCacheDir(context))
            .compress(compress)
            .loadImageEngine(GlideEngine.createGlideEngine())// 自定义图片加载引擎
            .compressSavePath(UriUtils.getCacheDir(context))
            .forResult(requestCode)
    }

}