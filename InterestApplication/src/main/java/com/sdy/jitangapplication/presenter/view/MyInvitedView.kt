package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.MyInvitedBeans

/**
 *    author : ZFM
 *    date   : 2020/7/3116:28
 *    desc   :
 *    version: 1.0
 */
interface MyInvitedView : BaseView {
    fun myinviteLogResult(data: MyInvitedBeans?)

}