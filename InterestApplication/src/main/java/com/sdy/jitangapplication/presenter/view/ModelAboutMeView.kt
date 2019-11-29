package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.LabelQualityBean

/**
 *    author : ZFM
 *    date   : 2019/11/117:28
 *    desc   :
 *    version: 1.0
 */
interface ModelAboutMeView : BaseView {

    fun getSignTemplateResult(code: Int, result: MutableList<LabelQualityBean>?)


    fun getTagTraitInfoResult(b: Boolean, mutableList: MutableList<LabelQualityBean>?)
}