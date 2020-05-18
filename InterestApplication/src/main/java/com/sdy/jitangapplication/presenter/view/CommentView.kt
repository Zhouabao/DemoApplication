package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.ProductCommentBean

/**
 *    author : ZFM
 *    date   : 2020/3/2517:54
 *    desc   :
 *    version: 1.0
 */
interface CommentView : BaseView {

    fun onGoodscommentsList(b: Boolean, data: MutableList<ProductCommentBean>?)
}