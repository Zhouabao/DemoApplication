package com.sdy.jitangapplication.presenter.view

import com.sdy.jitangapplication.model.Photos
import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2019/7/2114:42
 *    desc   :
 *    version: 1.0
 */
interface BlockSquareView :BaseView {

    fun getBlockSquareResult(success:Boolean,data:MutableList<Photos>?)

}