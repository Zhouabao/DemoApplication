package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.RecommendSquareListBean
import com.sdy.jitangapplication.model.SquareBannerBean

interface RecommendSquareView : BaseView {
    fun onGetSquareRecommendResult(data: RecommendSquareListBean?, b: Boolean)
    fun onCheckBlockResult(banner: SquareBannerBean, b: Boolean)

}