package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView

interface ContentView : BaseView {
    /**
     * 设置用户封禁
     */
    fun onCheckBlockResult(result: Boolean)


    /**
     * 广场发布结果
     */
    fun onSquareAnnounceResult(type: Int, b: Boolean, code: Int = 0)

    /**
     * 七牛上传进度
     */
    fun onQnUploadResult(b: Boolean, type: Int, key: String?)

}