package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.LoginBean
import com.sdy.jitangapplication.model.NewLabel

interface LabelsView : BaseView {

    fun onGetLabelsResult(labels: MutableList<NewLabel>)




    /**
     * 标签上传结果
     */
    fun onUploadLabelsResult(result: Boolean, userBean: LoginBean?)


}