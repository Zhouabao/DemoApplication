package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.HelpBean
import com.sdy.jitangapplication.presenter.view.LoginHelpView

/**
 *    author : ZFM
 *    date   : 2019/11/815:48
 *    desc   :
 *    version: 1.0
 */
class LoginHelpPresenter : BasePresenter<LoginHelpView>() {
    fun getHelpCenter() {
        RetrofitFactory.instance.create(Api::class.java)
            .getHelpCenter()
            .excute(object : BaseSubscriber<BaseResp<HelpBean?>>(mView) {
                override fun onNext(t: BaseResp<HelpBean?>) {
                    mView.getHelpCenterResult(t.code == 200, t.data)
                    if (t.code != 200) {
                        CommonFunction.toast(t.msg)
                    }
                }

                override fun onError(e: Throwable?) {
                    mView.getHelpCenterResult(false, null)
                    CommonFunction.toast(CommonFunction.getErrorMsg(context))

                }
            })
    }
}