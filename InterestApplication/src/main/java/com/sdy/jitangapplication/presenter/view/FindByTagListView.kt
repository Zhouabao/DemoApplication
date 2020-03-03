package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.AddSinlgLabelBean
import com.sdy.jitangapplication.model.FindByTagBean

interface FindByTagListView : BaseView {

    fun onGetTitleInfoResult(b: Boolean, matchBeans: FindByTagBean?)

    fun onAddLabelResult(result: Boolean, data: AddSinlgLabelBean?)

}