package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.TagSquareListBean

interface TagDetailCategoryView : BaseView {

    fun onCheckBlockResult(b: Boolean)


    fun onGetSquareRecommendResult(data: TagSquareListBean?, b: Boolean)

}