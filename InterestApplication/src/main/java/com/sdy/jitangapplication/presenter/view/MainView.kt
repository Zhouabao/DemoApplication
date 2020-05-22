package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.AllMsgCount
import com.sdy.jitangapplication.model.NearCountBean

/**
 *    author : ZFM
 *    date   : 2019/6/2515:30
 *    desc   :
 *    version: 1.0
 */
interface MainView : BaseView {

    fun onMsgListResult(allMsgCount: AllMsgCount?)


    fun startupRecordResult(data: NearCountBean?)
}