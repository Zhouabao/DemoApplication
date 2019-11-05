package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.NewJobBean

/**
 *    author : ZFM
 *    date   : 2019/8/119:29
 *    desc   :
 *    version: 1.0
 */
interface MyJobView:BaseView {
    fun onGetJobListResult(mutableList: MutableList<NewJobBean>?)
    fun onSavePersonal(b: Boolean)
}