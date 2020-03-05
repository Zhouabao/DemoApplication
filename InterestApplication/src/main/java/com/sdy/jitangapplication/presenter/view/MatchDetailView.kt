package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.MatchBean

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



}