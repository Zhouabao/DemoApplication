package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView

interface UserNickNameView : BaseView {

    //上传用户信息结果
    fun onUploadUserInfoResult(uploadResult: Boolean, msg: String?)

}