package com.example.demoapplication.presenter.view

import com.example.demoapplication.model.FriendBean
import com.example.demoapplication.model.SquareListBean
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2019/6/2420:26
 *    desc   :
 *    version: 1.0
 */
interface SquareView : BaseView {

    fun onGetFriendsListResult(friends: MutableList<FriendBean?>)

    fun onGetSquareListResult(data: SquareListBean?, result: Boolean, isRefresh: Boolean)

    fun onGetSquareLikeResult(position: Int, result: Boolean)

    fun onGetSquareCollectResult(position: Int, result: BaseResp<Any?>)



    fun onGetSquareReport(baseResp: BaseResp<Any?>, position: Int)
}