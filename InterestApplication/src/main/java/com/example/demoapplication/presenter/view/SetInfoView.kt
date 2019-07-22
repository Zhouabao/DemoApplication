package com.example.demoapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView

interface SetInfoView : BaseView {

    //上传用户信息结果
    fun onUploadUserInfoResult(uploadResult:Boolean)

    /**
     * 检查用户昵称结果
     */
    fun onCheckNickNameResult()


}