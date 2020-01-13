package com.sdy.jitangapplication.presenter

import com.blankj.utilcode.util.SPUtils
import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.presenter.view.LoginView

class LoginPresenter : BasePresenter<LoginView>() {

    /**
     * 验证昵称是否合法
     */
    fun checkNickName() {
        RetrofitFactory.instance
            .create(Api::class.java)
            .checkNickName()
            .excute(object : BaseSubscriber<BaseResp<Array<String>>>(mView) {
                override fun onNext(t: BaseResp<Array<String>>) {
                    super.onNext(t)
                    if (t.code == 200) {
                        var sensitive = ""
                        for (char in t.data) {
                            sensitive = sensitive.plus(char)
                        }
                        SPUtils.getInstance(Constants.SPNAME).put("sensitive", sensitive)
                    }
                }
            })
    }

    /**
     *
     */
    fun login(mobile: String, pwd: String, pushId: String) {
        if (!checkNetWork()) {
            return
        }
        mView.showLoading()

    }


}
