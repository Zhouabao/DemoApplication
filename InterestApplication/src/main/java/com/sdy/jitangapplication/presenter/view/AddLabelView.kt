package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.AddLabelBean
import com.sdy.jitangapplication.model.TagBean

/**
 *    author : ZFM
 *    date   : 2019/11/269:34
 *    desc   :
 *    version: 1.0
 */
interface AddLabelView : BaseView {

    fun onTagClassifyListResult(result: Boolean, data: AddLabelBean?)

    fun saveMyTagResult(result: Boolean, data: MutableList<TagBean>?)

}