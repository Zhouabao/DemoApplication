package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.MoreMatchBean
import com.sdy.jitangapplication.model.MyTapsBean

/**
 *    author : ZFM
 *    date   : 2020/5/714:11
 *    desc   :
 *    version: 1.0
 */
interface GetRelationshipView : BaseView {

    fun onAddWant(b: Boolean, data: MoreMatchBean?)

    fun onGetMyTaps(data: MutableList<MyTapsBean>)
}