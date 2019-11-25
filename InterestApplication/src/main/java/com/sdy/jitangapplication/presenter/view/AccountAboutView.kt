package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.AccountBean

/**
 *    author : ZFM
 *    date   : 2019/8/19:45
 *    desc   :
 *    version: 1.0
 */
interface AccountAboutView : BaseView {

    fun getAccountResult(accountBean: AccountBean)


    fun unbundWeChatResult(result: Boolean)
}