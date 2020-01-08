package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.MatchBean
import com.sdy.jitangapplication.model.StatusBean

/**
 *    author : ZFM
 *    date   : 2019/6/2610:48
 *    desc   :
 *    version: 1.0
 */
interface MatchDetailView : BaseView {

    fun onGetMatchDetailResult(success: Boolean, matchUserDetailBean: MatchBean?)

    fun onGetUserActionResult(success: Boolean, result: String?)


    fun onGetLikeResult(success: Boolean, statusBean: BaseResp<StatusBean?>?, islike: Boolean = true)

    fun onRemoveBlockResult(success: Boolean)



}