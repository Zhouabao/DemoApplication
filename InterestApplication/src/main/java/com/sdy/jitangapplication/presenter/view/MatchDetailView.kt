package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.MatchBean
import com.sdy.jitangapplication.model.RecommendSquareListBean

/**
 *    author : ZFM
 *    date   : 2019/6/2610:48
 *    desc   :
 *    version: 1.0
 */
interface MatchDetailView : BaseView {

    fun onGetMatchDetailResult(success: Boolean, matchUserDetailBean: MatchBean?)

    fun onGetUserActionResult(success: Boolean, result: String?)



    fun onRemoveBlockResult(success: Boolean)


    fun onNeedNoticeResult(success: Boolean)



    /**
     * 获取广场列表
     */
    fun onGetSquareListResult(data: RecommendSquareListBean?, result: Boolean, isRefresh: Boolean = false)

    //约会点赞结果
    fun doLikeResult(b: Boolean, isliked: Boolean)

}