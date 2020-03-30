package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.AddressBean

/**
 *    author : ZFM
 *    date   : 2020/3/2614:06
 *    desc   :
 *    version: 1.0
 */
interface AddAddressView : BaseView {
    fun onAddAddressResult(success: Boolean, address: AddressBean?)
}