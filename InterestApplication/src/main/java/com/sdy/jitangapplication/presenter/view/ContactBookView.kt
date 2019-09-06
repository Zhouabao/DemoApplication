package com.sdy.jitangapplication.presenter.view

import com.sdy.jitangapplication.model.ContactDataBean
import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2019/8/613:59
 *    desc   :
 *    version: 1.0
 */
interface ContactBookView:BaseView {
    fun onGetContactListResult(data: ContactDataBean?)
}