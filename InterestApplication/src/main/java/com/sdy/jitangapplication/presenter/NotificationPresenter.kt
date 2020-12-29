package com.sdy.jitangapplication.presenter

import android.app.Activity
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.SettingsBean
import com.sdy.jitangapplication.presenter.view.NotificationView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/8/19:44
 *    desc   :
 *    version: 1.0
 */
class NotificationPresenter : BasePresenter<NotificationView>() {
    /**
     * 用户广场点赞/评论接收推送开关 参数 type（int）型    1点赞    2评论
     */
    fun squareNotifySwitch(type: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .squareNotifySwitch(UserManager.getSignParams(hashMapOf("type" to type)))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    CommonFunction.toast(t.msg)
                    mView.onGreetApproveResult(type, t.code == 200)
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    }
                }
            })
    }


    /**
     * type  1短信 2隐身 3私聊  4微信公众号
     * state  	短信(1开启 2关闭)
     *          隐身（1 不隐身 2离线隐身 3一直隐身 ）
     */
    fun switchSet(type: Int, state: Int) {
        RetrofitFactory.instance.create(Api::class.java)
            .switchSet(UserManager.getSignParams(hashMapOf("type" to type, "state" to state)))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    CommonFunction.toast(t.msg)
                    mView.switchSetResult(type, t.code == 200)

                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                }


            })

    }


    /**
     * 获取我的设置
     */
    fun mySettings() {
        RetrofitFactory.instance.create(Api::class.java)
            .mySettings(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<SettingsBean?>>(mView) {
                override fun onNext(t: BaseResp<SettingsBean?>) {
                    mView.onSettingsBeanResult(t.code == 200, t.data)
                    if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)

                    } else if (t.code != 200) {
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {

                        mView.onSettingsBeanResult(false, null)
                        CommonFunction.toast(CommonFunction.getErrorMsg(context))
                    }

                }
            })
    }
}