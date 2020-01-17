package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.AddLabelResultBean
import com.sdy.jitangapplication.model.LabelQualitysBean

/**
 *    author : ZFM
 *    date   : 2019/11/2611:59
 *    desc   :
 *    version: 1.0
 */
interface LabelQualityView : BaseView {

    fun getQualityResult(result: Boolean, data: LabelQualitysBean?)

    fun addTagResult(result: Boolean, loginBean: AddLabelResultBean?)

}