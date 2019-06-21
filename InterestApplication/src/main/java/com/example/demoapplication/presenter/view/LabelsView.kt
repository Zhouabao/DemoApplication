package com.example.demoapplication.presenter.view

import com.example.demoapplication.model.Label
import com.kotlin.base.presenter.view.BaseView

interface LabelsView : BaseView {

    fun onGetLabelsResult(labels: MutableList<Label>?)


    /**
     * 获取子级标签，并记录其父级标签的位置
     */
    fun onGetSubLabelsResult(labels:MutableList<Label>?,parentPosition:Int)
}