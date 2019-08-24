package com.example.demoapplication.presenter.view

import com.example.demoapplication.model.BlackBean
import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2019/8/2319:30
 *    desc   :
 *    version: 1.0
 */
interface BlackListView : BaseView {
    fun onMyShieldingListResult(data: MutableList<BlackBean>?)

    fun onRemoveBlockResult(success: Boolean,position: Int)
}