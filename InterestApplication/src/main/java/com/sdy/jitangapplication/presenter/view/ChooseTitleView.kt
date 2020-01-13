package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.LabelQualityBean

interface ChooseTitleView : BaseView {
    fun getTagTraitInfoResult(b: Boolean, mutableList: MutableList<LabelQualityBean>?)
}