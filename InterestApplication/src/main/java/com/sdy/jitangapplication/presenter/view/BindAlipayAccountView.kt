package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.Alipay

/**
 *    author : ZFM
 *    date   : 2020/3/2417:05
 *    desc   :
 *    version: 1.0
 */
interface BindAlipayAccountView : BaseView {
    fun saveWithdrawAccountResult(success: Boolean, data: Alipay?)
}