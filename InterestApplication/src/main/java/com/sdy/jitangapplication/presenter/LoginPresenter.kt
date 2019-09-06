package com.sdy.jitangapplication.presenter

import android.util.Log
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.presenter.view.LoginView
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber

class LoginPresenter : BasePresenter<LoginView>() {


    /**
     *
     */
    fun login(mobile: String, pwd: String, pushId: String) {
        if (!checkNetWork()) {
            return
        }
        mView.showLoading()

    }


    /**
     * 获取验证码
     */
    fun getVerifyCode(mobile: String) {
        if (!checkNetWork()) {
            return
        }
        RetrofitFactory.instance
            .create(Api::class.java)
            .getVerifyCode(mobile, "register")
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    Log.i("retrofit", t.toString())
                    super.onNext(t)
                }
            })


    }
}
