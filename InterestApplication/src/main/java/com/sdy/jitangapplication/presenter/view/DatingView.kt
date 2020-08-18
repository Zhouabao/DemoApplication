package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.CheckBean
import com.sdy.jitangapplication.model.DatingBean
import com.sdy.jitangapplication.model.SquareTagBean

interface DatingView : BaseView {
    /**
     * 获取广场列表
     */
    fun onGetSquareDatingResult(data: MutableList<DatingBean>?, result: Boolean)


    fun onGetIntentionResult(result: MutableList<CheckBean>?)

}