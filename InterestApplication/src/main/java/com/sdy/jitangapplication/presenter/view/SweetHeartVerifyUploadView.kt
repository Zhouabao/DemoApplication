package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2020/9/179:23
 *    desc   :
 *    version: 1.0
 */
interface SweetHeartVerifyUploadView : BaseView {
    fun uploadImgResult(success: Boolean, key: String, index: Int)

    fun uploadDataResult(success: Boolean)

    fun getPicTplResult(datas: ArrayList<String>)
}