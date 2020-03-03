package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.AddLabelBean

interface FindByTagView : BaseView {
    fun onTagClassifyListResult(b: Boolean, data: AddLabelBean?)
}