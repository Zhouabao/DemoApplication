package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.ChargeWayBeans

/**
 *    author : ZFM
 *    date   : 2020/6/2917:20
 *    desc   :
 *    version: 1.0
 */
interface ChargeVipView : BaseView {
    fun giftRechargeListResult(data: ChargeWayBeans?)
}