package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.GoodsCategoryBeans
import com.sdy.jitangapplication.model.PullWithdrawBean

/**
 *    author : ZFM
 *    date   : 2020/3/2410:17
 *    desc   :
 *    version: 1.0
 */
interface MyCandyView : BaseView {

    fun ongoodsCategoryList(success: Boolean, data: GoodsCategoryBeans?)

    fun onMyCadnyResult(candyCount: PullWithdrawBean?)
}