package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.ProductDetailBean

/**
 *    author : ZFM
 *    date   : 2020/3/2515:32
 *    desc   :
 *    version: 1.0
 */
interface CandyProductDetailView : BaseView {
    fun onGoodsInfoResult(data: ProductDetailBean?)

    fun onGoodsAddWishResult(goodsListBean: Boolean)

}