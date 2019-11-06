package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.presenter.view.BaseView
import com.netease.nimlib.sdk.auth.LoginInfo
import com.sdy.jitangapplication.model.LoginBean

interface VerifyCodeView : BaseView {

    //对比验证码是否正确,存储登录数据
    fun onConfirmVerifyCode(data: LoginBean,success:Boolean)


    //获取验证码结果
    fun onGetVerifyCode(data: BaseResp<Any?>)


    //倒计时
    fun onCountTime()

    //获取手机号码
    fun onGetPhoneNum()

    fun onIMLoginResult(nothing: LoginInfo?, success: Boolean)

}