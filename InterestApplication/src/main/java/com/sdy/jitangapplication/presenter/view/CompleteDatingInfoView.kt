package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.DatingOptionsBean

/**
 *    author : ZFM
 *    date   : 2020/8/1214:19
 *    desc   :
 *    version: 1.0
 */
interface CompleteDatingInfoView : BaseView {

    fun onDatingOptionsResult(data: DatingOptionsBean?)


    fun onDatingReleaseResult(success: Boolean, code: Int = 0)

    fun onQnUploadResult(success: Boolean, key: String)
}