package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.AddLabelBean

/**
 *    author : ZFM
 *    date   : 2019/11/269:34
 *    desc   :
 *    version: 1.0
 */
interface AddLabelView : BaseView {

    fun onTagClassifyListResult(result: Boolean, data: AddLabelBean?)
}