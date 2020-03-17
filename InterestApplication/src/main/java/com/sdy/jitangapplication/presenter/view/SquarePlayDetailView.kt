package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.SquareBean

/**
 *    author : ZFM
 *    date   : 2019/7/417:57
 *    desc   :
 *    version: 1.0
 */
interface SquarePlayDetailView : BaseView {
    fun onGetSquareInfoResults(mutableList: SquareBean?)

    fun onGetSquareLikeResult(position: Int, result: Boolean)

    fun onGetSquareCollectResult(position: Int, data: BaseResp<Any?>)

    fun onAddCommentResult(position: Int, data: BaseResp<Any?>?, result: Boolean)
    fun onRemoveMySquareResult(b: Boolean, position: Int)
    fun onGetSquareReport(t: Boolean)

}