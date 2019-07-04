package com.example.demoapplication.presenter

import android.util.Log
import com.example.demoapplication.api.Api
import com.example.demoapplication.model.CheckBean
import com.example.demoapplication.presenter.view.VerifyCodeView
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber

class VerifyCodePresenter : BasePresenter<VerifyCodeView>() {


    /**
     * 对比验证码是否正确
     */
    fun checkVerifyCode(phone: String, verifyCode: String) {
        RetrofitFactory.instance.create(Api::class.java)
            .checkVerifyCode(phone, verifyCode,"register")
            .excute(object : BaseSubscriber<BaseResp<CheckBean>>(mView) {
                override fun onNext(t: BaseResp<CheckBean>) {
                    super.onNext(t)
                    Log.i("retrofit", t.toString())
                    if (t.data.check) {
                        mView.onConfirmVerifyCode(true)
                    } else {
                        mView.onConfirmVerifyCode(false)
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
                    mView.onGetVerifyCode(t)
                }
            })


    }
}