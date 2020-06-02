package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.RegisterFileBean

interface LoginView : BaseView {

    fun onGetRegisterProcessType(data: RegisterFileBean?)

}