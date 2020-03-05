package com.sdy.jitangapplication.presenter

import android.app.Activity
import android.app.Dialog
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.MatchBean
import com.sdy.jitangapplication.model.StatusBean
import com.sdy.jitangapplication.presenter.view.MatchDetailView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager
import com.sdy.jitangapplication.widgets.CommonAlertDialog

/**
 *    author : ZFM
 *    date   : 2019/6/2610:48
 *    desc   :
 *    version: 1.0
 */
class MatchDetailPresenter : BasePresenter<MatchDetailView>() {


    /**
     * 获取详细的用户数据 包括用户的广场信息
     */
    fun getUserDetailInfo(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .getMatchUserInfo(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<MatchBean?>>(mView) {
                override fun onStart() {
                }

                override fun onNext(t: BaseResp<MatchBean?>) {
                    if (t.code == 200) {
                        mView.onGetMatchDetailResult(true, t.data)
                    } else if (t.code == 409) {//用户被封禁
                        CommonAlertDialog.Builder(context)
                            .setTitle("提示")
                            .setContent(t.msg)
                            .setCancelIconIsVisibility(false)
                            .setConfirmText("知道了")
                            .setCancelAble(false)
                            .setOnConfirmListener(object : CommonAlertDialog.OnConfirmListener {
                                override fun onClick(dialog: Dialog) {
                                    dialog.cancel()
                                    NIMClient.getService(MsgService::class.java).deleteRecentContact2(params["target_accid"].toString(), SessionTypeEnum.P2P)
                                    (context as Activity).finish()
                                }
                            })
                            .create()
                            .show()
                    } else {
                        mView.onGetMatchDetailResult(false, null)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onGetMatchDetailResult(false, null)
                }
            })

    }


    /**
     * 拉黑用户
     */
    fun shieldingFriend(params: HashMap<String, Any>) {
        if (!checkNetWork()) {
            return
        }
        RetrofitFactory.instance.create(Api::class.java)
            .shieldingFriend(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                }

                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        mView.onGetUserActionResult(true, t.msg)
                    } else {
                        CommonFunction.toast(t.msg)
                        mView.onGetUserActionResult(false, "")
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onError(CommonFunction.getErrorMsg(context))
                        mView.onGetUserActionResult(false, null)
                    }
                }
            })

    }

    /**
     * 解除拉黑
     */
    fun removeBlock(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .removeBlock(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        mView.onRemoveBlockResult(true)
                    } else {
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onRemoveBlockResult(false)
                    }
                }
            })
    }


    /*
     * 解除匹配
     */
    fun dissolutionFriend(params: HashMap<String, Any>) {
        if (!checkNetWork()) {
            return
        }

        RetrofitFactory.instance.create(Api::class.java)
            .dissolutionFriend(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                }

                override fun onNext(t: BaseResp<Any?>) {
                    if (t.code == 200) {
                        mView.onGetUserActionResult(true, t.msg)
                    } else {
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onError(context.getString(R.string.service_error))
                        mView.onGetUserActionResult(false, null)
                    }
                }
            })

    }
}