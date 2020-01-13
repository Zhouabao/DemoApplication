package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.loginOffCauseBean

/**
 *    author : ZFM
 *    date   : 2019/8/19:45
 *    desc   :
 *    version: 1.0
 */
interface ChangeAccountView : BaseView {

    fun onChangeAccountResult(result: Boolean)

    fun onSendSmsResult(result: Boolean)

    fun onCauseListResult(result: loginOffCauseBean)

}