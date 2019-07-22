package com.example.demoapplication.presenter.view

import com.example.demoapplication.model.MatchBean
import com.example.demoapplication.model.StatusBean
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2019/6/2610:48
 *    desc   :
 *    version: 1.0
 */
interface MatchDetailView : BaseView {

    fun onGetMatchDetailResult(success: Boolean, matchUserDetailBean: MatchBean?)

    fun onGetUserActionResult(success: Boolean, result: String?)


    fun onGetLikeResult(success: Boolean, statusBean: BaseResp<StatusBean?>?)
}