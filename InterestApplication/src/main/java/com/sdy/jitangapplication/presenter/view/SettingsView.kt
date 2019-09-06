package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2019/8/19:45
 *    desc   :
 *    version: 1.0
 */
interface SettingsView : BaseView {

    fun onBlockedAddressBookResult(success: Boolean)

    fun onHideDistanceResult(success: Boolean)
}