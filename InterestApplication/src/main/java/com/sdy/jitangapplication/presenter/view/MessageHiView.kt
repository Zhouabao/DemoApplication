package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.presenter.view.BaseView
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.sdy.jitangapplication.model.HiMessageBean

/**
 *    author : ZFM
 *    date   : 2019/8/510:54
 *    desc   :
 *    version: 1.0
 */
interface MessageHiView : BaseView {
    fun onGreatListResult(t: BaseResp<MutableList<HiMessageBean>?>)

    fun onDelTimeoutGreetResult(t: Boolean, accids: MutableList<String>?)


    fun onGetRecentContactResults(result: MutableList<RecentContact>, t: BaseResp<MutableList<HiMessageBean>?>)
}