package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.VideoVerifyBannerBean

/**
 *    author : ZFM
 *    date   : 2020/5/289:20
 *    desc   :
 *    version: 1.0
 */
interface VideoVerifyView : BaseView {
    fun onUpdateFaceInfo(code: Int)


    fun onGetMvNormalCopy(code: MutableList<VideoVerifyBannerBean>)
}