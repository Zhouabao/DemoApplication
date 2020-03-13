package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.RecommendSquareListBean

interface RecommendSquareView : BaseView {
    fun onGetSquareRecommendResult(data: RecommendSquareListBean?, b: Boolean)
}