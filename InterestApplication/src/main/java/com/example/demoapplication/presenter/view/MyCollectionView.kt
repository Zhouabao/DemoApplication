package com.example.demoapplication.presenter.view

import com.example.demoapplication.model.SquareListBean
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2019/7/319:40
 *    desc   :
 *    version: 1.0
 */
interface MyCollectionView : BaseView {


    fun onGetSquareLikeResult(position: Int, result: Boolean)

    fun onGetSquareCollectResult(position: Int, result: BaseResp<Any?>?)

    fun onGetSquareReport(baseResp: BaseResp<Any?>?, position: Int)

    fun onGetSquareListResult(data: SquareListBean?)

    fun onRemoveMySquareResult(result: Boolean,position:Int)
}