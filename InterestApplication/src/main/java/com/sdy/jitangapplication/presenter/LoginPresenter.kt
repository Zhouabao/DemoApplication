package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.RegisterFileBean
import com.sdy.jitangapplication.presenter.view.LoginView

class LoginPresenter : BasePresenter<LoginView>() {

    /**
     * 获取登录配置开关
     */
    fun getRegisterProcessType() {
        RetrofitFactory.instance
            .create(Api::class.java)
            .getRegisterProcessType()
            .excute(object : BaseSubscriber<BaseResp<RegisterFileBean?>>(mView) {
                override fun onNext(t: BaseResp<RegisterFileBean?>) {
                    super.onNext(t)
                    mView.onGetRegisterProcessType(t.data)
                }
            })
    }




}
