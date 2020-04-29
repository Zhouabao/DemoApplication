package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.AllMsgCount
import com.sdy.jitangapplication.model.IndexRecommendBean
import com.sdy.jitangapplication.model.InvestigateBean
import com.sdy.jitangapplication.model.NearCountBean
import com.sdy.jitangapplication.presenter.view.MainView
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/6/2515:30
 *    desc   :
 *    version: 1.0
 */
class MainPresenter : BasePresenter<MainView>() {


    /**
     * 更新条件筛选
     */
    fun msgList() {
        RetrofitFactory.instance.create(Api::class.java)
            .msgList(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<AllMsgCount?>>(mView) {
                override fun onNext(t: BaseResp<AllMsgCount?>) {
                    if (t.code == 200) {
                        mView.onMsgListResult(t.data)
                    }
                }

                override fun onError(e: Throwable?) {

                }
            })


    }


    /**
     * 启动统计
     */
    fun startupRecord(token: String, accid: String, province_name: String?, city_name: String?) {
        val params = UserManager.getBaseParams()
        params["province_name"] = province_name ?: ""
        params["city_name"] = city_name ?: ""
        RetrofitFactory.instance.create(Api::class.java)
            .startupRecord(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<NearCountBean?>>(mView) {
                override fun onNext(t: BaseResp<NearCountBean?>) {
                    mView.startupRecordResult(t.data)
                }

                override fun onError(e: Throwable?) {

                }
            })


    }


    /**
     * 调查问卷请求
     */
    fun getQuestion(token: String, accid: String) {
        RetrofitFactory.instance.create(Api::class.java)
            .getQuestion(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<InvestigateBean?>>(mView) {
                override fun onNext(t: BaseResp<InvestigateBean?>) {
                    if (t.code == 200 && t.data != null) {
                        mView.onInvestigateResult(t.data!!)
                    }
                }

                override fun onError(e: Throwable?) {

                }

            })
    }


    /**
     * 今日推荐
     */
    fun todayRecommend() {
        RetrofitFactory.instance.create(Api::class.java)
            .todayRecommend(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<MutableList<IndexRecommendBean>?>>(mView) {
                override fun onNext(t: BaseResp<MutableList<IndexRecommendBean>?>) {
                    mView.onTodayRecommend(t.data)
                }

                override fun onError(e: Throwable?) {

                }

            })
    }
}