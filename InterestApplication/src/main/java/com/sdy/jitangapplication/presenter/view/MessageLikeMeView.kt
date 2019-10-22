package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.LikeMeBean

/**
 *    author : ZFM
 *    date   : 2019/8/510:54
 *    desc   :
 *    version: 1.0
 */
interface MessageLikeMeView : BaseView {
    fun onLikeListsResult(freeshow: Boolean, mutableList: MutableList<LikeMeBean>)

}