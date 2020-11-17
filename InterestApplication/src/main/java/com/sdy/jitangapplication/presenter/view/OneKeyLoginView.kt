package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.netease.nimlib.sdk.auth.LoginInfo
import com.sdy.jitangapplication.model.LoginBean
import com.sdy.jitangapplication.model.RegisterFileBean

interface OneKeyLoginView : BaseView {

    fun onConfirmVerifyCode(data: LoginBean?, b: Boolean)
    fun onIMLoginResult(param: LoginInfo?, b: Boolean)

}