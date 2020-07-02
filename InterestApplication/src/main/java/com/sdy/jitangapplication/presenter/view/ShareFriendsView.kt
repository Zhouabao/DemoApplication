package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.MyInviteBean

/**
 *    author : ZFM
 *    date   : 2020/7/19:27
 *    desc   :
 *    version: 1.0
 */
interface ShareFriendsView : BaseView {

    fun myInviteResult(myInviteBean: MyInviteBean?)
}