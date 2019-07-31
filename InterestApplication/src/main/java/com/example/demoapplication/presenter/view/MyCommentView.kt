package com.example.demoapplication.presenter.view

import com.example.demoapplication.model.AllCommentBean
import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2019/7/3120:55
 *    desc   :
 *    version: 1.0
 */
interface MyCommentView : BaseView {
    fun onGetCommentListResult(data: AllCommentBean?, refresh: Boolean)
}