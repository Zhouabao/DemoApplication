package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.LabelQualityBean

interface MyIntentionView : BaseView {

    fun onGetIntentionListResult(data: MutableList<LabelQualityBean>?)

    fun onSaveRegisterInfo(success: Boolean)
}