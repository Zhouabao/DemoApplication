package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.IndexListBean
import com.sdy.jitangapplication.presenter.view.IndexView
import com.sdy.jitangapplication.utils.UserManager

class IndexPresenter : BasePresenter<IndexView>() {

    /**
     * 获取首页推荐十个
     */
    fun indexTop() {
        if (!UserManager.touristMode)
            RetrofitFactory.instance.create(Api::class.java)
                .indexTop(UserManager.getSignParams())
                .excute(object : BaseSubscriber<BaseResp<IndexListBean?>>(mView) {
                    override fun onNext(t: BaseResp<IndexListBean?>) {
                        super.onNext(t)
                        mView.indexTopResult(t.data)
                    }

                    override fun onError(e: Throwable?) {
                        super.onError(e)
                        mView.indexTopResult(null)
                    }
                })
        else
            RetrofitFactory.instance.create(Api::class.java)
                .indexTopThreshold(UserManager.getSignParams())
                .excute(object : BaseSubscriber<BaseResp<IndexListBean?>>(mView) {
                    override fun onNext(t: BaseResp<IndexListBean?>) {
                        super.onNext(t)
                        mView.indexTopResult(t.data)
                    }

                    override fun onError(e: Throwable?) {
                        super.onError(e)
                        mView.indexTopResult(null)
                    }
                })
    }

}