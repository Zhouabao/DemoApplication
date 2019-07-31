package com.example.demoapplication.presenter.view

import com.example.demoapplication.model.VisitorBean
import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2019/7/319:40
 *    desc   :
 *    version: 1.0
 */
interface MyVisitView : BaseView {

    fun onMyVisitResult(visitor: MutableList<VisitorBean>?)
}