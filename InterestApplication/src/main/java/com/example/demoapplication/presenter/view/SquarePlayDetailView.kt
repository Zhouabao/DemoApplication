package com.example.demoapplication.presenter.view

import com.example.demoapplication.model.SquareBean
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2019/7/417:57
 *    desc   :
 *    version: 1.0
 */
interface SquarePlayDetailView : BaseView {
    fun onGetRecentlySquaresResults(mutableList: MutableList<SquareBean?>)

    fun onGetSquareLikeResult(position: Int, result: Boolean)

    fun onGetSquareCollectResult(position: Int, data: BaseResp<Any?>)

    fun onAddCommentResult(position: Int, data: BaseResp<Any?>)

}