package com.example.demoapplication.presenter.view

import com.example.demoapplication.model.AllMsgCount
import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2019/6/2515:30
 *    desc   :
 *    version: 1.0
 */
interface MainView : BaseView {

    fun onMsgListResult(allMsgCount: AllMsgCount?)
}