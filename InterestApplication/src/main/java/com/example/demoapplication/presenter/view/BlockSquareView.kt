package com.example.demoapplication.presenter.view

import com.example.demoapplication.model.Photos
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