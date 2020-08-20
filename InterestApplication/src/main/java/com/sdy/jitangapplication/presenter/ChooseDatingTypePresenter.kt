package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.CheckBean
import com.sdy.jitangapplication.model.DatingOptionsBean
import com.sdy.jitangapplication.presenter.view.ChooseDatingTypeView
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/8/1010:30
 *    desc   :
 *    version: 1.0
 */
class ChooseDatingTypePresenter : BasePresenter<ChooseDatingTypeView>() {

    /**
     * 获取约会类型
     */
    fun getIntention() {
        RetrofitFactory.instance.create(Api::class.java)
            .datingOptions(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<DatingOptionsBean?>>() {
                override fun onNext(t: BaseResp<DatingOptionsBean?>) {
                    super.onNext(t)
                    mView.onGetIntentionResult(t.data)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    mView.onGetIntentionResult(null)
                }
            })
    }
}