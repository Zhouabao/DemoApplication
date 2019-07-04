package com.example.demoapplication.presenter.view

import com.example.demoapplication.R
import com.kotlin.base.presenter.view.BaseView

interface SetInfoView : BaseView {

    //切换用户性别
    fun onChangeSex(id: Int = R.id.userSexWoman)

    //填写用户生日
    fun onChangeBirth()

    //上传用户信息结果
    fun onUploadUserInfoResult()

    /**
     * 检查用户昵称结果
     */
    fun onCheckNickNameResult(result: Boolean)


}