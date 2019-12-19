package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.SamePersonListBean

interface SquareSamePersonView : BaseView {

    fun onGetTitleInfoResult(b: Boolean, data: SamePersonListBean?)

    fun onCheckBlockResult(b: Boolean)

}