package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.AllMsgCount
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
        if (UserManager.touristMode)
            return
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
    fun startupRecord(province_name: String?, city_name: String?) {
        val params = UserManager.getBaseParams()
        params["province_name"] = province_name ?: ""
        params["city_name"] = city_name ?: ""
        RetrofitFactory.instance.create(Api::class.java)
            .startupRecord(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<NearCountBean?>>(mView) {
                override fun onNext(t: BaseResp<NearCountBean?>) {
                }

                override fun onError(e: Throwable?) {

                }
            })


    }


}