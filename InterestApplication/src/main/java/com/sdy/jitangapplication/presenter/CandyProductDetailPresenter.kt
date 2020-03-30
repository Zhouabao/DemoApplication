package com.sdy.jitangapplication.presenter

import com.kotlin.base.data.net.RetrofitFactory
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.ext.excute
import com.kotlin.base.presenter.BasePresenter
import com.kotlin.base.rx.BaseException
import com.kotlin.base.rx.BaseSubscriber
import com.sdy.jitangapplication.api.Api
import com.sdy.jitangapplication.model.ProductDetailBean
import com.sdy.jitangapplication.presenter.view.CandyProductDetailView
import com.sdy.jitangapplication.ui.dialog.TickDialog
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2020/3/2515:32
 *    desc   :
 *    version: 1.0
 */
class CandyProductDetailPresenter : BasePresenter<CandyProductDetailView>() {

    /**
     * 商品详情
     */
    fun goodsInfo(goods_id: Int) {
        val params = hashMapOf<String, Any>()
        params["goods_id"] = goods_id
        RetrofitFactory.instance.create(Api::class.java)
            .goodsInfo(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<ProductDetailBean?>>(mView) {
                override fun onNext(t: BaseResp<ProductDetailBean?>) {
                    super.onNext(t)
                    mView.onGoodsInfoResult(t.data)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    if (e is BaseException) {
                        TickDialog(context).show()
                    }
                }
            })
    }


    /**
     * 商品加入心愿单
     */
    fun goodsAddWish(id: Int) {
        val params = hashMapOf<String, Any>()
        params["goods_id"] = id
        RetrofitFactory.instance.create(Api::class.java)
            .goodsAddWish(UserManager.getSignParams(params))
            .excute(object : BaseSubscriber<BaseResp<Any?>>(mView) {
                override fun onNext(t: BaseResp<Any?>) {
                    super.onNext(t)
                    mView.onGoodsAddWishResult(t.code == 200)
                }

                override fun onError(e: Throwable?) {
                    super.onError(e)
                    if (e is BaseException) {
                        TickDialog(context).show()
                    } else
                        mView.onGoodsAddWishResult(false)
                }
            })

    }
}