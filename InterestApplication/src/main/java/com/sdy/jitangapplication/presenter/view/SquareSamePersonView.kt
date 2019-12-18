package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.SamePersonBean

interface SquareSamePersonView : BaseView {

    fun onGetTitleInfoResult(b: Boolean, data: MutableList<SamePersonBean>?)

    fun onCheckBlockResult(b: Boolean)

}