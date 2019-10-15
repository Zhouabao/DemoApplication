package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.LoginBean
import com.sdy.jitangapplication.model.NewLabel

interface NewLabelsView : BaseView {

    fun onGetLabelsResult(data: MutableList<NewLabel>)


    fun onUploadLabelsResult(success: Boolean, data: LoginBean?)

}