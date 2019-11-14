package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2019/10/3119:11
 *    desc   :
 *    version: 1.0
 */
interface LoginHelpResonView : BaseView {

    fun onGetUserActionResult(b: Boolean, msg: String?)

    fun onGetReportMsgResult(b: Boolean, msg: MutableList<String>?)

    fun uploadImgResult(success: Boolean, imageName: String, index: Int)


}