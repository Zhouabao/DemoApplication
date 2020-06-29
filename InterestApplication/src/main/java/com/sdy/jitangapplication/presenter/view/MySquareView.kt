package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.RecommendSquareListBean

/**
 *    author : ZFM
 *    date   : 2019/7/319:40
 *    desc   :
 *    version: 1.0
 */
interface MySquareView : BaseView {
    fun onGetSquareListResult(data: RecommendSquareListBean?, b: Boolean)

    fun onCheckBlockResult(b: Boolean)

//    fun onSquareAnnounceResult(i: Int, b: Boolean, code: Int)

//    fun onQnUploadResult(b: Boolean, type: Int, key: String?)

}