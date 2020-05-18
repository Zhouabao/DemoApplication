package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.MyLikedBean

/**
 *    author : ZFM
 *    date   : 2019/7/3120:55
 *    desc   :
 *    version: 1.0
 */
interface MyLikedView : BaseView {
    fun onGetCommentListResult(data: MutableList<MyLikedBean>?, refresh: Boolean)

}