package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.InvitePoliteBean
import com.sdy.jitangapplication.presenter.view.InviteRewardsView
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/8/310:15
 *    desc   :
 *    version: 1.0
 */
class InviteRewardsPresenter : BasePresenter<InviteRewardsView>() {

    fun invitePolite() {
        RetrofitFactory.instance.create(Api::class.java)
            .invitePolite(UserManager.getSignParams())
            .excute(object :BaseSubscriber<BaseResp<InvitePoliteBean?>>(mView){
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<InvitePoliteBean?>) {
                    super.onNext(t)
                    mView.invitePoliteResult(t.data)

                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                }
            })
    }
}