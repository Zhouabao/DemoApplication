package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.Alipay
import com.sdy.jitangapplication.presenter.view.BindAlipayAccountView
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/3/2417:05
 *    desc   :
 *    version: 1.0
 */
class BindAlipayAccountPresenter : BasePresenter<BindAlipayAccountView>() {

    /**
     * 保存支付宝账号
     */
    fun saveWithdrawAccount(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .saveWithdrawAccount(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Alipay?>>(mView) {

                override fun onNext(t: BaseResp<Alipay?>) {
                    super.onNext(t)
                    mView.saveWithdrawAccountResult(t.code == 200,t.data)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    mView.saveWithdrawAccountResult(false,null)
                }
            })
    }
}