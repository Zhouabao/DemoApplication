package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2019/11/113:56
 *    desc   :
 *    version: 1.0
 */
interface ChooseSchoolView : BaseView {
    fun onGetSchoolListResult(success: Boolean, schoolList: MutableList<String>?)


    fun onGetJobListResult(mutableList: MutableList<String>?)
}