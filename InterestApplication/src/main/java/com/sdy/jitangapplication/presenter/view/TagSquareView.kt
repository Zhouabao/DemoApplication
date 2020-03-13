package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.SquareTagBean

interface TagSquareView : BaseView {
    /**
     * 获取广场列表
     */
    fun onGetSquareTagResult(data: MutableList<SquareTagBean>?, result: Boolean)

}