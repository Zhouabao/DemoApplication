package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.SomeOneGetGiftBean

/**
 *    author : ZFM
 *    date   : 2020/4/214:49
 *    desc   :
 *    version: 1.0
 */
interface SomeoneGetGiftView : BaseView {

    fun onGetSomeoneGiftList(success: Boolean, data: MutableList<SomeOneGetGiftBean>?)
}