package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.DatingOptionsBean
import com.sdy.jitangapplication.presenter.view.CompleteDatingInfoView
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/8/1214:19
 *    desc   :
 *    version: 1.0
 */
class CompleteDatingInfoPresenter : BasePresenter<CompleteDatingInfoView>() {

    /**
     * 发布约会
     */
    fun releaseDate() {
        RetrofitFactory.instance.create(Api::class.java)
            .releaseDate(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onStart() {
                    super.onStart()
                }

                override fun onCompleted() {
                    super.onCompleted()
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                }


            })
    }


    /**
     * 获取发布选项
     */
    fun datingOptions() {
        RetrofitFactory.instance.create(Api::class.java)
            .datingOptions(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<DatingOptionsBean?>>(mView) {
                override fun onStart() {
                    super.onStart()
                }

                override fun onNext(t: BaseResp<DatingOptionsBean?>) {
                    super.onNext(t)
                    mView.onDatingOptionsResult(t.data)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                }


            })
    }
}