package com.example.demoapplication.presenter

import com.example.demoapplication.api.Api
import com.example.demoapplication.model.UserInfoBean
import com.example.demoapplication.presenter.view.UserCenterView
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber

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
            .myInfo(params)
            .excute(object : BaseSubscriber<BaseResp<UserInfoBean?>>(mView) {
                override fun onNext(t: BaseResp<UserInfoBean?>) {
                    mView.onGetMyInfoResult(t.data)
                }

                override fun onError(e: Throwable?) {
                    mView.onError("")
                }
            })
    }



}