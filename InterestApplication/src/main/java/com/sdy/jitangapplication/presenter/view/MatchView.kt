package com.sdy.jitangapplication.presenter.view

import com.sdy.jitangapplication.model.GreetBean
import com.sdy.jitangapplication.model.MatchBean
import com.sdy.jitangapplication.model.MatchListBean
import com.sdy.jitangapplication.model.StatusBean
import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2019/6/2117:26
 *    desc   :
 *    version: 1.0
 */
interface MatchView : BaseView {

    fun onGetMatchListResult(success: Boolean, matchBeans: MatchListBean?)

    fun onGetDislikeResult(success: Boolean,data: StatusBean?)

    fun onGetLikeResult(success: Boolean, data: StatusBean?)

    fun onGreetStateResult(greetBean: GreetBean?, matchBean: MatchBean)


    fun onGreetSResult(greetBean: Boolean)
}