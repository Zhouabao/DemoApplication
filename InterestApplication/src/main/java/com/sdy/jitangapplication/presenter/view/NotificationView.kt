package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.SettingsBean

/**
 *    author : ZFM
 *    date   : 2019/8/19:45
 *    desc   :
 *    version: 1.0
 */
interface NotificationView : BaseView {


    fun onGreetApproveResult(type: Int, success: Boolean)
    fun switchSetResult(type: Int, success: Boolean)


    fun onSettingsBeanResult(success: Boolean, settingsBean: SettingsBean?)


}