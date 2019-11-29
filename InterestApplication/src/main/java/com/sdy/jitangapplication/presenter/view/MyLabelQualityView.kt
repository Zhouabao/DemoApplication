package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.model.LoginBean

/**
 *    author : ZFM
 *    date   : 2019/11/2611:59
 *    desc   :
 *    version: 1.0
 */
interface MyLabelQualityView : BaseView {
    fun getTagTraitInfoResult(type: Int, result: Boolean, data: MutableList<LabelQualityBean>?)

    fun addTagResult(result: Boolean, data: LoginBean?)

    fun delTagResult(result: Boolean)
}