package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.CheckBean
import com.sdy.jitangapplication.model.DatingOptionsBean

/**
 *    author : ZFM
 *    date   : 2020/8/1010:30
 *    desc   :
 *    version: 1.0
 */
interface ChooseDatingTypeView : BaseView {

    fun onGetIntentionResult(result: DatingOptionsBean?)

}