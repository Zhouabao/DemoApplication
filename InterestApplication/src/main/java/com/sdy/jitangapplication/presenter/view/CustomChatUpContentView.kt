package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2019/11/2611:59
 *    desc   :
 *    version: 1.0
 */
interface CustomChatUpContentView : BaseView {
    fun onSaveChatupMsg(success: Boolean, msg: String)
}