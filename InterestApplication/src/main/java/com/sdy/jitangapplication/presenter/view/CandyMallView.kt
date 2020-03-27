package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.GoodsListBean

/**
 *    author : ZFM
 *    date   : 2020/3/2510:37
 *    desc   :
 *    version: 1.0
 */
interface CandyMallView : BaseView {

    fun onGoodsListResult(goodsListBean: GoodsListBean?)
}