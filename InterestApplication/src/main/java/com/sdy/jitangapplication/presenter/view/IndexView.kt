package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.IndexListBean

interface IndexView : BaseView {

    fun indexTopResult(data: IndexListBean?)
}