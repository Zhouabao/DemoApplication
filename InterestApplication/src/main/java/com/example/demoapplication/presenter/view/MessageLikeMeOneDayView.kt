package com.example.demoapplication.presenter.view

import com.example.demoapplication.model.LikeMeBean
import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2019/8/510:54
 *    desc   :
 *    version: 1.0
 */
interface MessageLikeMeOneDayView:BaseView {
    fun onLikeListResult(datas: MutableList<LikeMeBean>)
}