package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.UserInfoBean

interface UserCenterView : BaseView {

    fun onGetMyInfoResult(userinfo: UserInfoBean?)


}