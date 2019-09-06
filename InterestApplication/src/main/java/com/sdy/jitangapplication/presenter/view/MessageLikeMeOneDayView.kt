package com.sdy.jitangapplication.presenter.view

import com.sdy.jitangapplication.model.LikeMeOneDayBean
import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2019/8/510:54
 *    desc   :
 *    version: 1.0
 */
interface MessageLikeMeOneDayView:BaseView {
    fun onLikeListResult(datas: MutableList<LikeMeOneDayBean>)
}