package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2019/7/814:53
 *    desc   :
 *    version: 1.0
 */
interface PublishView : BaseView {

    fun onSquareAnnounceResult(type: Int, success: Boolean)

    fun onQnUploadResult(success: Boolean, type: Int, key: String)
}