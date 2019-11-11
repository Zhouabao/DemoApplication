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
import com.sdy.jitangapplication.model.UserInfoBean
import com.sdy.jitangapplication.presenter.view.UserCenterView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/7/299:19
 *    desc   :
 *    version: 1.0
 */
class UserCenterPresenter : BasePresenter<UserCenterView>() {

    //获取个人信息
    fun getMemberInfo(params: HashMap<String, Any>) {
        if (!checkNetWork()) {
            mView.onError("")
        }
        RetrofitFactory.instance.create(Api::class.java)
            .myInfo(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<UserInfoBean?>>(mView) {
                override fun onNext(t: BaseResp<UserInfoBean?>) {
                        mView.onGetMyInfoResult(t.data)
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onError("")
                }
            })
    }

    /**
     * 获取广场列表
     */
    fun checkBlock(token: String, accid: String) {
        RetrofitFactory.instance.create(Api::class.java)
            .checkBlock(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    if (t.code == 200)
                        mView.onCheckBlockResult(true)
                    else if (t.code == 403) {
                        UserManager.startToLogin(context as Activity)
                    } else {
                        CommonFunction.toast(t.msg)
                        mView.onCheckBlockResult(false)
                    }
                }

                override fun onError(e: Throwable?) {
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else {
                        mView.onError("服务器错误~")
                        mView.onCheckBlockResult(false)
                    }
                }
            })
    }


}