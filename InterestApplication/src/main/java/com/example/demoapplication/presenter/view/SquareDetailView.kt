package com.example.demoapplication.presenter.view

import com.example.demoapplication.model.AllCommentBean
import com.example.demoapplication.model.GreetBean
import com.example.demoapplication.model.SquareBean
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2019/6/2717:22
 *    desc   :
 *    version: 1.0
 */
interface SquareDetailView : BaseView {

    fun onGetCommentListResult(allCommentBean: AllCommentBean?, refresh: Boolean)

    fun onGetSquareLikeResult(result: Boolean)

    fun onLikeCommentResult(data: BaseResp<Any?>,position:Int)

    fun onDeleteCommentResult(data: BaseResp<Any?>,position:Int)

    fun onReportCommentResult(data: BaseResp<Any?>,position:Int)

    fun onGetSquareCollectResult(data: BaseResp<Any?>?)

    fun onGetSquareReport(data: BaseResp<Any?>?)

    fun onAddCommentResult(data: BaseResp<Any?>?, result: Boolean)

    fun onRemoveMySquareResult(b: Boolean)

    fun onGetSquareInfoResults(data: SquareBean?)

    fun onGreetSResult(b: Boolean)

    fun onGreetStateResult(data: GreetBean?)
}