package com.sdy.jitangapplication.common

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ToastUtils
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.nim.activity.ChatActivity
import com.sdy.jitangapplication.ui.activity.MainActivity
import com.sdy.jitangapplication.ui.dialog.ChargeVipDialog
import com.sdy.jitangapplication.ui.dialog.HarassmentDialog
import com.sdy.jitangapplication.ui.dialog.SayHiDialog
import com.sdy.jitangapplication.utils.UriUtils
import com.sdy.jitangapplication.utils.UserManager
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.WXAPIFactory

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
        if (isfriend || isgreeted) {
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
                ChargeVipDialog(ChargeVipDialog.DOUBLE_HI, context, ChargeVipDialog.PURCHASE_GREET_COUNT).show()
            }
        }
        view.postDelayed({ view.isEnabled = true }, 500L)
    }


    fun dissolveRelationship(target_accid: String) {
        NIMClient.getService(MsgService::class.java).deleteRecentContact2(target_accid, SessionTypeEnum.P2P)
        // 删除与某个聊天对象的全部消息记录
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
            CommonFunction.toast("你没有安装微信")
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
    fun onTakePhoto(context: Context, maxCount: Int, requestCode: Int, chooseMode: Int = 1) {
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
            .previewImage(true)
            .previewVideo(true)
            .isCamera(true)
            .enableCrop(false)
            .compressSavePath(UriUtils.getCacheDir(context))
            .compress(false)
            .minimumCompressSize(100)
            .scaleEnabled(true)
            .showCropFrame(true)
            .rotateEnabled(false)
            .withAspectRatio(9, 16)
            .compressSavePath(UriUtils.getCacheDir(context))
            .openClickSound(false)
            .forResult(requestCode)
    }

}