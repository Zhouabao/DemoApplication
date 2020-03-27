package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.GoodsListBean
import com.sdy.jitangapplication.presenter.view.CandyMallView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/3/2510:37
 *    desc   :
 *    version: 1.0
 */
class CandyMallPresenter : BasePresenter<CandyMallView>() {

    /**
     * 获取商品列表
     */
    fun goodsList() {
        RetrofitFactory.instance.create(Api::class.java)
            .goodsList(UserManager.getSignParams())
            .excute(object : BaseSubscriber<BaseResp<GoodsListBean?>>(mView) {
                override fun onNext(t: BaseResp<GoodsListBean?>) {
                    super.onNext(t)
                    mView.onGoodsListResult(t.data)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    if (e is BaseException) {
                        TickDialog(context).show()
                    }
                }
            })

    }
}