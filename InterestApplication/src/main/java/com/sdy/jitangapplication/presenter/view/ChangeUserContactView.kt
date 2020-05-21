package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.ContactWayBean

/**
 *    author : ZFM
 *    date   : 2020/5/2015:42
 *    desc   :
 *    version: 1.0
 */
interface ChangeUserContactView : BaseView {
    fun onGetContactResult(data: ContactWayBean?)

    fun onSetContactResult(success: Boolean)
}