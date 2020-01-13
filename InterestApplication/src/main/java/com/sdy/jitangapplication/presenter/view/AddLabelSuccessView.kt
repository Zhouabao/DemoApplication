package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.LabelQualityBean

interface AddLabelSuccessView : BaseView {

    fun getTagTraitInfoResult(b: Boolean, mutableList: MutableList<LabelQualityBean>?)

    fun onSquareAnnounceResult(b: Boolean, code: Int = 0)

    fun onUploadImgResult(b: Boolean, qnPath: String)

}