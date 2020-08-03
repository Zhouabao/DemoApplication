package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.InvitePoliteBean

/**
 *    author : ZFM
 *    date   : 2020/8/310:15
 *    desc   :
 *    version: 1.0
 */
interface InviteRewardsView : BaseView {

    fun invitePoliteResult(invitePoliteBean: InvitePoliteBean?)
}