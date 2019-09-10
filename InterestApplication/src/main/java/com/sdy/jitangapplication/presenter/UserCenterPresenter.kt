package com.sdy.jitangapplication.presenter

import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.UserInfoBean
import com.sdy.jitangapplication.presenter.view.UserCenterView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
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
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onError("")
                }
            })
    }


}