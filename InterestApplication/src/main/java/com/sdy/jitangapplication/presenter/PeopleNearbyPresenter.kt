package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.NearBean
import com.sdy.jitangapplication.model.TodayFateBean
import com.sdy.jitangapplication.presenter.view.PeopleNearbyView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.ui.fragment.PeopleNearbyFragment
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/4/2710:11
 *    desc   :
 *    version: 1.0
 */
class PeopleNearbyPresenter : BasePresenter<PeopleNearbyView>() {

    /**
     * 获取首页附近的人
     */
    fun nearlyIndex(params: HashMap<String, Any>, type: Int) {
        //游客模式则提醒登录
        if (UserManager.touristMode) {
            RetrofitFactory.instance.create(Api::class.java)
                .thresholdIndex(UserManager.getSignParams(params))
                .excute(object : BaseSubscriber<BaseResp<NearBean?>>(mView) {
                    override fun onNext(t: BaseResp<NearBean?>) {
                        super.onNext(t)
                        mView.nearlyIndexResult(t.code == 200, t.data)
                    }

                    override fun onError(e: Throwable?) {
                        super.onError(e)
                        if (e is BaseException) {
                            TickDialog(context).show()
                        } else
                            mView.nearlyIndexResult(false, null)
                    }
                })
        } else
            when (type) {
                PeopleNearbyFragment.TYPE_RECOMMEND -> {
                    RetrofitFactory.instance.create(Api::class.java)
                        .recommendIndex(UserManager.getSignParams(params))
                        .excute(object : BaseSubscriber<BaseResp<NearBean?>>(mView) {
                            override fun onNext(t: BaseResp<NearBean?>) {
                                super.onNext(t)
                                mView.nearlyIndexResult(t.code == 200, t.data)
                            }

                            override fun onError(e: Throwable?) {
                                super.onError(e)
                                if (e is BaseException) {
                                    TickDialog(context).show()
                                } else
                                    mView.nearlyIndexResult(false, null)
                            }
                        })
                }
                PeopleNearbyFragment.TYPE_SAMECITY -> {
                    RetrofitFactory.instance.create(Api::class.java)
                        .theSameCity(UserManager.getSignParams(params))
                        .excute(object : BaseSubscriber<BaseResp<NearBean?>>(mView) {
                            override fun onNext(t: BaseResp<NearBean?>) {
                                super.onNext(t)
                                mView.nearlyIndexResult(t.code == 200, t.data)
                            }

                            override fun onError(e: Throwable?) {
                                super.onError(e)
                                if (e is BaseException) {
                                    TickDialog(context).show()
                                } else
                                    mView.nearlyIndexResult(false, null)
                            }
                        })
                }
            }

    }


    /**
     * 获取今日缘分
     */
    fun todayRecommend() {
        RetrofitFactory.instance.create(Api::class.java)
            .todayRecommend(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<TodayFateBean?>>() {
                override fun onNext(t: BaseResp<TodayFateBean?>) {
                    mView.onTodayRecommendResult(t.data)
                }

                override fun onError(e: Throwable?) {
                    mView.onTodayRecommendResult(null)
                }

            })
    }
}