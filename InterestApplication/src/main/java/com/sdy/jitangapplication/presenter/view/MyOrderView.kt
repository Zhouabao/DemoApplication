package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.MyOrderBean

/**
 *    author : ZFM
 *    date   : 2020/3/2616:33
 *    desc   :
 *    version: 1.0
 */
interface MyOrderView : BaseView {

    fun onMyGoodsList(success: Boolean, mutableList: MutableList<MyOrderBean>?)
}