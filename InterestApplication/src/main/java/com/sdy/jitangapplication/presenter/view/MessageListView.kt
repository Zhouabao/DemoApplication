package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.sdy.jitangapplication.model.MessageListBean1

/**
 *    author : ZFM
 *    date   : 2019/8/510:54
 *    desc   :
 *    version: 1.0
 */
interface MessageListView : BaseView {
    fun onMessageCensusResult(data: MessageListBean1?)

    fun updateOfflineContactAited(recentAited: MutableList<RecentContact>)

    fun onGetRecentContactResults(result: MutableList<RecentContact>)
}