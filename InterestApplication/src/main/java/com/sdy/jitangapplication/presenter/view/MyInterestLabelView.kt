package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.LabelQualityBean

/**
 *    author : ZFM
 *    date   : 2019/11/279:48
 *    desc   :
 *    version: 1.0
 */
interface MyInterestLabelView : BaseView {
    fun getMyTagsListResult(result: Boolean, data:MutableList<LabelQualityBean>?)


    fun delTagResult(result: Boolean, position: Int)


}