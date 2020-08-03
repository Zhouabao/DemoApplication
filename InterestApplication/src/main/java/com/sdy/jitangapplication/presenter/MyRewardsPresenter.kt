package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.MyInvitedBeans
import com.sdy.jitangapplication.presenter.view.MyInvitedView
import com.sdy.jitangapplication.presenter.view.MyRewardsView
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/7/3116:28
 *    desc   :
 *    version: 1.0
 */
class MyRewardsPresenter : BasePresenter<MyRewardsView>() {

    fun myinviteLog(page:Int){
        RetrofitFactory.instance.create(Api::class.java)
            .myinviteLog(UserManager.getSignParams(hashMapOf("page" to page,"pagesize" to Constants.PAGESIZE)))
            .excute(object : BaseSubscriber<BaseResp<MyInvitedBeans?>>(mView){
                override fun onNext(t: BaseResp<MyInvitedBeans?>) {
                    super.onNext(t)
                    mView.myinviteLogResult(t.data)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    mView.myinviteLogResult(null)

                }
            })
    }
}