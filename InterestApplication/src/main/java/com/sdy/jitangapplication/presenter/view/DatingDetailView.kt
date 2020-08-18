package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.DatingBean

/**
 *    author : ZFM
 *    date   : 2020/8/1711:49
 *    desc   :
 *    version: 1.0
 */
interface DatingDetailView : BaseView {
    fun datingInfoResult(datingBean: DatingBean?)


    fun doLikeResult(code: Boolean,isLiked:Boolean)
}