package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.MyAddressBean

/**
 *    author : ZFM
 *    date   : 2020/4/717:14
 *    desc   :
 *    version: 1.0
 */
interface AddressManagerView : BaseView {
    fun getAddressResult(data: MyAddressBean?)

    fun delAddressResult(success: Boolean, position: Int)

    fun defaultAddressResult(success: Boolean, position: Int)

}