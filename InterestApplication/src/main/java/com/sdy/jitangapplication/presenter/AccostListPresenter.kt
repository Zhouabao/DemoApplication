package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.AccostBean
import com.sdy.jitangapplication.presenter.view.AccostListView

/**
 *    author : ZFM
 *    date   : 2020/6/914:25
 *    desc   :
 *    version: 1.0
 */
class AccostListPresenter : BasePresenter<AccostListView>() {

    /**
     * 搭讪列表
     */
    fun chatupList(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .chatupList(params)
            .excute(object : BaseSubscriber<BaseResp<MutableList<AccostBean>?>>(mView) {
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<MutableList<AccostBean>?>) {
                    super.onNext(t)
                    mView.onChatupListResult(t.data)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                }
            })
    }

}