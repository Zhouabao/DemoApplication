package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.presenter.view.BaseView
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.sdy.jitangapplication.model.GreetedListBean

interface GreetReceivedView : BaseView {

    fun onGreatListResult(t: BaseResp<MutableList<GreetedListBean>?>)


    fun onLikeOrGreetStateResult(result: Boolean, type: Int)


    fun onGetRecentContactResults(result: MutableList<RecentContact>)


}