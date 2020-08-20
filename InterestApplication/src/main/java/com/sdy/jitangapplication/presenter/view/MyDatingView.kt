package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.DatingBean

/**
 *    author : ZFM
 *    date   : 2019/7/319:40
 *    desc   :
 *    version: 1.0
 */
interface MyDatingView : BaseView {
    fun onGetMyDatingResult(data: MutableList<DatingBean>?)
}