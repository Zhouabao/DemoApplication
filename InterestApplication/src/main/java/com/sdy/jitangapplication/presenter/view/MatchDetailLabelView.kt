package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.OtherLabelsBean

/**
 *    author : ZFM
 *    date   : 2019/11/2816:20
 *    desc   :
 *    version: 1.0
 */
interface MatchDetailLabelView : BaseView {

    fun getOtherTagsResult(result: Boolean, data: OtherLabelsBean?)
}