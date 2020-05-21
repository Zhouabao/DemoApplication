package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.IndexRecommendBean
import com.sdy.jitangapplication.model.NearBean

/**
 *    author : ZFM
 *    date   : 2020/4/2710:11
 *    desc   :
 *    version: 1.0
 */
interface PeopleNearbyView : BaseView {

    fun nearlyIndexResult(success: Boolean, mutableList: NearBean?)

    fun onTodayRecommendResult(data: MutableList<IndexRecommendBean>?)

}