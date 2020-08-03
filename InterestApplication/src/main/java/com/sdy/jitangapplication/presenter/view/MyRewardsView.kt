package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.MyInvitedBeans
import com.sdy.jitangapplication.model.MyRewardBeans

/**
 *    author : ZFM
 *    date   : 2020/7/3116:28
 *    desc   :
 *    version: 1.0
 */
interface MyRewardsView : BaseView {
    fun myInviteRewardResult(data: MyRewardBeans?)
}