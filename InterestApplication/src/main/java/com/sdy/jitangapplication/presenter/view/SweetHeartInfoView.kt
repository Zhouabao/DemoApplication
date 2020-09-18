package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.SquareBean

/**
 *    author : ZFM
 *    date   : 2020/9/1517:23
 *    desc   :
 *    version: 1.0
 */
interface SweetHeartInfoView : BaseView {
    fun onGetSquareInfoResults(data: SquareBean?)
}