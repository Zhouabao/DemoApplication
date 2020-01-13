package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.MyLabelsBean
import com.sdy.jitangapplication.model.TagBean

/**
 *    author : ZFM
 *    date   : 2019/11/279:48
 *    desc   :
 *    version: 1.0
 */
interface MyLabelView : BaseView {
    fun getMyTagsListResult(result: Boolean, datas: MyLabelsBean?)


    fun delTagResult(result: Boolean, position: Int, data: MutableList<TagBean>?)

}