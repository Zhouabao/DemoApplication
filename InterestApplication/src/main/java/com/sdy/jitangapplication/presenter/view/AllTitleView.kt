package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.LabelQualityBean
import com.sdy.jitangapplication.model.TopicBean

interface AllTitleView : BaseView {

    fun onGetTitleMenuListResult(b: Boolean, data: MutableList<LabelQualityBean>?)

    fun onGetTitleListsResult(b: Boolean, data: MutableList<TopicBean>?)
}