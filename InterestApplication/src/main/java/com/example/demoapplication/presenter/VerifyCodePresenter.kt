package com.example.demoapplication.presenter

import com.example.demoapplication.presenter.view.VerifyCodeView
import com.kotlin.base.presenter.BasePresenter

class VerifyCodePresenter : BasePresenter<VerifyCodeView>() {


    /**
     * 对比验证码是否正确
     */
    fun getVerifyCode(verifyCode: String) {
        if (verifyCode == "000000") {
            mView.onConfirmVerifyCode(true)
        } else {
            mView.onConfirmVerifyCode(false)
        }


//        RetrofitFactory.instance.create(Api::class.java)
//            .getVerifyCode("", "", "")
//            .excute(object : BaseSubscriber<BaseResp<String>>(mView) {
//                override fun onNext(t: BaseResp<String>) {
//                    super.onNext(t)
//                }
//            })
    }
}