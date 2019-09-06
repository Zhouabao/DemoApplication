package com.sdy.jitangapplication.presenter.view

import com.sdy.jitangapplication.model.UserInfoBean
import com.kotlin.base.presenter.view.BaseView

interface UserCenterView : BaseView {

    fun onGetMyInfoResult(userinfo: UserInfoBean?)

}