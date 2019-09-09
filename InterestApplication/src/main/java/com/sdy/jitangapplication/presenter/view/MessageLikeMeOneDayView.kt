package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.LikeMeOneDayBean
import com.sdy.jitangapplication.model.StatusBean

/**
 *    author : ZFM
 *    date   : 2019/8/510:54
 *    desc   :
 *    version: 1.0
 */
interface MessageLikeMeOneDayView : BaseView {
    fun onLikeListResult(datas: MutableList<LikeMeOneDayBean>)


    fun onGetLikeResult(b: Boolean, t: BaseResp<StatusBean?>, position: Int)
}