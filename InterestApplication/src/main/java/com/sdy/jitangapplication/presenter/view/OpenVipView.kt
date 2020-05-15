package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.ChargeWayBeans

/**
 *    author : ZFM
 *    date   : 2020/5/716:26
 *    desc   :
 *    version: 1.0
 */
interface OpenVipView : BaseView {
    fun onProductListsResult(data: ChargeWayBeans)
}