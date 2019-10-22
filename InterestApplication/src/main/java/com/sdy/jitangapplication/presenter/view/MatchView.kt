package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.GreetBean
import com.sdy.jitangapplication.model.MatchBean
import com.sdy.jitangapplication.model.MatchListBean
import com.sdy.jitangapplication.model.StatusBean

/**
 *    author : ZFM
 *    date   : 2019/6/2117:26
 *    desc   :
 *    version: 1.0
 */
interface MatchView : BaseView {

    fun onGetMatchListResult(success: Boolean, matchBeans: MatchListBean?)

    fun onGetDislikeResult(success: Boolean, data: BaseResp<StatusBean?>)

    fun onGetLikeResult(success: Boolean, data: BaseResp<StatusBean?>, matchBean: MatchBean)

    fun onGreetStateResult(greetBean: GreetBean?, matchBean: MatchBean)


    fun onGreetSResult(greetBean: Boolean, code: Int, matchBean: MatchBean)
}