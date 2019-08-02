package com.example.demoapplication.presenter.view

import com.example.demoapplication.model.LabelBean
import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2019/8/119:29
 *    desc   :
 *    version: 1.0
 */
interface MyJobView:BaseView {
    fun onGetJobListResult(mutableList: MutableList<LabelBean>?)
    fun onSavePersonal(b: Boolean)
}