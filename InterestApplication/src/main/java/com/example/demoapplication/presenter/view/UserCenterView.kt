package com.example.demoapplication.presenter.view

import com.example.demoapplication.model.UserInfoBean
import com.kotlin.base.presenter.view.BaseView

interface UserCenterView : BaseView {

    fun onGetMyInfoResult(userinfo: UserInfoBean?)

}