package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.SquareListBean

/**
 *    author : ZFM
 *    date   : 2019/6/2420:26
 *    desc   :
 *    version: 1.0
 */
interface SquareView : BaseView {

    /**
     * 获取广场列表
     */
    fun onGetSquareListResult(data: SquareListBean?, result: Boolean, isRefresh: Boolean = false)

    /**
     * 设置用户封禁
     */
    fun onCheckBlockResult( result: Boolean)

    /**
     * 广场点赞
     */
    fun onGetSquareLikeResult(position: Int, result: Boolean)

    /**
     * 广场上收藏
     */
    fun onGetSquareCollectResult(position: Int, result: BaseResp<Any?>?)

    /**
     * 广场举报
     */
    fun onGetSquareReport(baseResp: BaseResp<Any?>?, position: Int)

    /**
     * 删除自己的广场动态
     */
    fun onRemoveMySquareResult(result: Boolean, position: Int)

    /**
     * 广场发布结果
     */
    fun onSquareAnnounceResult(type: Int, b: Boolean, code: Int = 0)

    /**
     * 七牛上传进度
     */
    fun onQnUploadResult(b: Boolean, type: Int, key: String?)



}