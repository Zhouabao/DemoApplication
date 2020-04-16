package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.WantFriendBean

/**
 *    author : ZFM
 *    date   : 2020/3/2517:37
 *    desc   :
 *    version: 1.0
 */
interface WantProductListView : BaseView {

    fun onGoodsWishList(success: Boolean, data: MutableList<WantFriendBean>?)

    fun onGiveGoods(success: Boolean)

}