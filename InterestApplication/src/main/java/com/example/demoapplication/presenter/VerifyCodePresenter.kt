package com.example.demoapplication.presenter

import android.util.Log
import com.example.demoapplication.api.Api
import com.example.demoapplication.model.LoginBean
import com.example.demoapplication.presenter.view.VerifyCodeView
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber

class VerifyCodePresenter : BasePresenter<VerifyCodeView>() {


    /**
     * 对比验证码是否正确，正确即登录
     */
    fun checkVerifyCode(phone: String, verifyCode: String) {
        if (!checkNetWork()) {
            return
        }
        RetrofitFactory.instance.create(Api::class.java)
            .loginOrAlloc(phone, code = verifyCode)
            .excute(object : BaseSubscriber<BaseResp<LoginBean>>(mView) {
                override fun onNext(t: BaseResp<LoginBean>) {
                    super.onNext(t)
                    if (t.code == 500) {
                        mView.onError(t.msg)
                    } else if (t.code == 200) {
                        mView.onConfirmVerifyCode(t.data, true)
                    } else {
                        mView.onConfirmVerifyCode(t.data, false)
                    }
                }
            })
    }


    /**
     * 重新获取验证码
     */
    fun getVerifyCode(mobile: String) {
        if (!checkNetWork()) {
            return
        }
        RetrofitFactory.instance
            .create(Api::class.java)
            .getVerifyCode(mobile, "register")
            .excute(object : BaseSubscriber<BaseResp<Array<String>?>>(mView) {
                override fun onNext(t: BaseResp<Array<String>?>) {
                    super.onNext(t)
                    Log.i("retrofit", t.toString())
                    if (t.code == 200)
                        mView.onGetVerifyCode(t)
                    else
                        mView.onError(t.msg)
                }
            })


    }
}