package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.ChargeWayBeans

interface VipPowerView : BaseView {
    fun getChargeDataResult(data: ChargeWayBeans?)
}