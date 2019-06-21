package com.example.demoapplication.presenter

import com.example.demoapplication.presenter.view.LoginView
import com.kotlin.base.presenter.BasePresenter

class LoginPresenter : BasePresenter<LoginView>(){


    /*
          登录
       */
    fun login(mobile: String, pwd: String, pushId: String) {
        if (!checkNetWork()) {
            return
        }
        mView.showLoading()

    }

}
