package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.LoginHelpBean

/**
 *    author : ZFM
 *    date   : 2019/11/815:48
 *    desc   :
 *    version: 1.0
 */
interface LoginHelpView : BaseView {

    fun getHelpCenterResult(success: Boolean, data: MutableList<LoginHelpBean>?)
}