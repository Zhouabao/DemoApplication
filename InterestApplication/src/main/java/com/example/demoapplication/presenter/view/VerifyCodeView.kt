package com.example.demoapplication.presenter.view

import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.presenter.view.BaseView

interface VerifyCodeView : BaseView {

    //改变验证码按钮的状态事件
    fun onChangeVerifyButtonStatus(enable: Boolean)


    //对比验证码是否正确
    fun onConfirmVerifyCode(isRight: Boolean)


    //获取验证码结果
    fun onGetVerifyCode(data: BaseResp<Array<String>?>)


    //倒计时
    fun onCountTime()

    //获取手机号码
    fun onGetPhoneNum()

}