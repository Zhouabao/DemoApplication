package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.sdy.jitangapplication.model.AccostBean

/**
 *    author : ZFM
 *    date   : 2020/6/914:25
 *    desc   :
 *    version: 1.0
 */
interface AccostListView : BaseView {

    fun onChatupListResult(data: MutableList<AccostBean>?)

    fun onGetRecentContactResults(
        result: MutableList<RecentContact>,
        data: MutableList<AccostBean>
    )

}