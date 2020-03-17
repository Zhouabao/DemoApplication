package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.NewLikeMeBean
import com.sdy.jitangapplication.model.StatusBean

interface LikeMeReceivedView : BaseView {

    fun onGreatListResult(t: BaseResp<NewLikeMeBean?>)

    fun onLikeOrGreetStateResult(result: BaseResp<StatusBean?>, type: Int)

    fun onGetDislikeResult(b: Boolean)


}