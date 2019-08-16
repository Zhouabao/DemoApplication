package com.example.demoapplication.presenter.view

import com.example.demoapplication.model.HiMessageBean
import com.kotlin.base.data.protocol.BaseResp
import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2019/8/510:54
 *    desc   :
 *    version: 1.0
 */
interface MessageHiView:BaseView {
    fun onGreatListResult(t: BaseResp<MutableList<HiMessageBean>?>)

    fun onDelTimeoutGreetResult(t: Boolean)
}