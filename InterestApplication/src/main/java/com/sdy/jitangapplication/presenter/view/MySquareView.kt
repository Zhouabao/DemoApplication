package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.RecommendSquareListBean
import com.sdy.jitangapplication.model.SquareListBean

/**
 *    author : ZFM
 *    date   : 2019/7/319:40
 *    desc   :
 *    version: 1.0
 */
interface MySquareView : BaseView {


//    fun onGetSquareLikeResult(position: Int, result: Boolean)

//    fun onGetSquareCollectResult(position: Int, result: BaseResp<Any?>?)

//    fun onGetSquareReport(baseResp: BaseResp<Any?>?, position: Int)

    fun onGetSquareListResult(data: SquareListBean?)


    fun onGetSquareRecommendResult(data: RecommendSquareListBean?, b: Boolean)


    fun onCheckBlockResult( b: Boolean)

//    fun onRemoveMySquareResult(result: Boolean,position:Int)
}