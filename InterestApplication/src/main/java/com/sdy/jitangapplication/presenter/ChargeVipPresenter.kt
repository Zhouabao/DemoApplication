package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.ChargeWayBeans
import com.sdy.jitangapplication.model.PaywayBean
import com.sdy.jitangapplication.presenter.view.ChargeVipView
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/6/2917:19
 *    desc   :
 *    version: 1.0
 */
class ChargeVipPresenter : BasePresenter<ChargeVipView>() {

    fun giftRechargeList() {
        RetrofitFactory.instance.create(Api::class.java)
            .candyRechargeList(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<ChargeWayBeans?>>(null) {
                override fun onNext(t: BaseResp<ChargeWayBeans?>) {
                    super.onNext(t)
                    mView.giftRechargeListResult(t.data)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                }
            })
    }

}