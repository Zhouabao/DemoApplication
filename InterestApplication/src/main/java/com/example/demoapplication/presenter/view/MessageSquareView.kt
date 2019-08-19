package com.example.demoapplication.presenter.view

import com.example.demoapplication.model.SquareMsgBean
import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2019/8/510:54
 *    desc   :
 *    version: 1.0
 */
interface MessageSquareView : BaseView {
    fun onSquareListsResult(data: MutableList<SquareMsgBean>?)

    fun onDelSquareMsgResult(success: Boolean)
}