package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.SquareLabelsBean

interface ChooseLabelView : BaseView {
    fun getSquareTagListResult(b: Boolean, data: SquareLabelsBean?)
}