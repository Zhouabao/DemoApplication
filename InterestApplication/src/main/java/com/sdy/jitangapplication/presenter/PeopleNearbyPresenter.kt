package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.NearPersonBean
import com.sdy.jitangapplication.presenter.view.PeopleNearbyView
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
    fun nearlyIndex(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .nearlyIndex(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<MutableList<NearPersonBean>?>>(mView) {
                override fun onNext(t: BaseResp<MutableList<NearPersonBean>?>) {
                    super.onNext(t)
                    mView.nearlyIndexResult(t.code == 200, t.data)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    CommonFunction.getErrorMsg(context)
                }
            })
    }

}