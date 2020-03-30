package com.sdy.jitangapplication.presenter.view

import com.kotlin.base.presenter.view.BaseView
import com.sdy.jitangapplication.model.BillBean

/**
 *    author : ZFM
 *    date   : 2020/3/259:43
 *    desc   :
 *    version: 1.0
 */
interface CandyRecordView : BaseView {

    fun onMyBillList(success: Boolean, billList: MutableList<BillBean>?)
}