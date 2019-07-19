package com.example.demoapplication.presenter.view

import com.example.demoapplication.model.MatchListBean
import com.kotlin.base.presenter.view.BaseView

/**
 *    author : ZFM
 *    date   : 2019/6/2117:26
 *    desc   :
 *    version: 1.0
 */
interface MatchView : BaseView {

    fun onGetMatchListResult(success: Boolean, matchBeans: MatchListBean?)
}