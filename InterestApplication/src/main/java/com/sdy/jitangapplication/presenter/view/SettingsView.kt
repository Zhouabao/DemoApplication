package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.SettingsBean
import com.sdy.jitangapplication.model.VersionBean

/**
 *    author : ZFM
 *    date   : 2019/8/19:45
 *    desc   :
 *    version: 1.0
 */
interface SettingsView : BaseView {

    fun onBlockedAddressBookResult(success: Boolean)

    fun onHideDistanceResult(success: Boolean)


    fun onGreetApproveResult(success: Boolean)

    fun onGreetSwitchResult(success: Boolean)


    fun onGetVersionResult(versionBean: VersionBean?)


    fun onSettingsBeanResult(success: Boolean, settingsBean: SettingsBean?)
}