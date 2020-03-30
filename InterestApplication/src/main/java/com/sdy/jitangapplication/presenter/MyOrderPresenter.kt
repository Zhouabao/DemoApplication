package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.MyOrderBean
import com.sdy.jitangapplication.presenter.view.MyOrderView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/3/2616:32
 *    desc   :
 *    version: 1.0
 */
class MyOrderPresenter : BasePresenter<MyOrderView>() {
    /**
     * 获取我的订单列表
     */
    fun myGoodsList(params: HashMap<String, Any>) {
        RetrofitFactory.instance.create(Api::class.java)
            .myGoodsList(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<MutableList<MyOrderBean>?>>(mView) {
                override fun onNext(t: BaseResp<MutableList<MyOrderBean>?>) {
                    super.onNext(t)
                    mView.onMyGoodsList(t.code == 200, t.data)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onMyGoodsList(false, null)
                }
            })
    }
}