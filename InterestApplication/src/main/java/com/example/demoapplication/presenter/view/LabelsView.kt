package com.example.demoapplication.presenter.view

import com.example.demoapplication.model.LabelBean
import com.example.demoapplication.model.LoginBean
import com.kotlin.base.presenter.view.BaseView

interface LabelsView : BaseView {

    fun onGetLabelsResult(labels: MutableList<LabelBean>)


    /**
     * 获取子级标签，并记录其父级标签的位置
     */
    fun onGetSubLabelsResult(labels: List<LabelBean>?, parentPosition: Int)


    /**
     * 清除子级标签
     */
    fun onRemoveSubLablesResult(labels: LabelBean, parentLevel: Int)


    /**
     * 标签上传结果
     */
    fun onUploadLabelsResult(result: Boolean, userBean: LoginBean?)


}